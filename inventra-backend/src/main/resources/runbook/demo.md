# Inventra Demo en AWS Lightsail

Runbook para desplegar **Inventra** como demo en una sola instancia AWS Lightsail.

---

## 1. Objetivo

Desplegar Inventra para demo, sin ambiente productivo, sin alta disponibilidad y sin sobrearquitectura.

La app queda así:

```text
http://TU_IP_PUBLICA/
  → Frontend React/Vite

http://TU_IP_PUBLICA/api/v1/...
  → Backend Spring Boot
```

Arquitectura:

```text
AWS Lightsail Ubuntu
  └── Docker Compose
      ├── postgres:16
      ├── backend Spring Boot Java 21
      └── nginx
          ├── sirve React build
          └── proxy /api → backend
```

No se exponen directamente:

```text
8080  backend
5432  postgres
```

Solo se abre al público:

```text
22  SSH
80  HTTP
443 HTTPS opcional después
```

---

## 2. Crear instancia Lightsail

En AWS Console:

```text
Amazon Lightsail
→ Create instance
→ Platform: Linux/Unix
→ Blueprint: OS Only
→ Ubuntu 24.04 LTS
```

Si no aparece Ubuntu 24.04:

```text
Ubuntu 22.04 LTS
```

Plan usado para este demo:

```text
4 GB RAM
2 vCPU
80 GB SSD
```

Nombre sugerido:

```text
inventra-demo
```

Después crear una **Static IP** y asignarla a la instancia.

Firewall Lightsail:

```text
Allow SSH  22
Allow HTTP 80
```

No abrir:

```text
8080
5432
```

---

## 3. Conectarse por SSH

Desde Lightsail:

```text
Instance
→ Connect using SSH
```

Actualizar sistema:

```bash
sudo apt update
sudo apt upgrade -y
```

---

## 4. Instalar Docker y herramientas base

Instalar dependencias:

```bash
sudo apt install -y ca-certificates curl gnupg lsb-release git unzip nginx
```

Instalar Docker:

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
```

Instalar Docker Compose plugin:

```bash
sudo apt install -y docker-compose-plugin
```

Aplicar grupo Docker:

```bash
newgrp docker
```

Validar instalación:

```bash
docker --version
docker compose version
```

Resultado esperado similar:

```text
Docker version 29.5.3
Docker Compose version v5.1.4
```

---

## 5. Crear estructura del demo

Parar Nginx del host para que no choque con el Nginx de Docker:

```bash
sudo systemctl stop nginx || true
sudo systemctl disable nginx || true
```

Crear carpetas:

```bash
sudo mkdir -p /opt/inventra/backend
sudo mkdir -p /opt/inventra/web/dist
sudo mkdir -p /opt/inventra/nginx
sudo chown -R ubuntu:ubuntu /opt/inventra

cd /opt/inventra
```

Estructura final:

```text
/opt/inventra
  ├── docker-compose.yml
  ├── .env
  ├── backend
  │   └── inventra-backend.jar
  ├── web
  │   └── dist/
  └── nginx
      └── default.conf
```

---

## 6. Crear variables `.env`

Dentro de `/opt/inventra`:

```bash
cd /opt/inventra

PUBLIC_IP=$(curl -s http://checkip.amazonaws.com | tr -d '\n')
POSTGRES_PASSWORD=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 64)

cat > .env <<EOF
POSTGRES_DB=inventra
POSTGRES_USER=inventra
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}

SPRING_PROFILES_ACTIVE=demo
JWT_SECRET=${JWT_SECRET}
CORS_ALLOWED_ORIGINS=http://${PUBLIC_IP}

JAVA_OPTS=-Xms256m -Xmx768m
EOF

cat .env
```

---

## 7. Crear configuración Nginx

Crear archivo:

```bash
cd /opt/inventra

cat > nginx/default.conf <<'EOF'
server {
    listen 80;
    server_name _;

    client_max_body_size 20m;

    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
```

---

## 8. Crear página temporal

Esto sirve para validar Nginx antes de subir React real.

```bash
cd /opt/inventra

cat > web/dist/index.html <<'EOF'
<!doctype html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Inventra Demo</title>
  </head>
  <body style="font-family: system-ui; padding: 40px;">
    <h1>Inventra Demo</h1>
    <p>Nginx está vivo. Falta subir React y backend.</p>
  </body>
</html>
EOF
```

---

## 9. Crear `docker-compose.yml`

Crear archivo:

```bash
cd /opt/inventra

cat > docker-compose.yml <<'EOF'
services:
  postgres:
    image: postgres:16
    container_name: inventra-postgres
    restart: unless-stopped
    env_file:
      - .env
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - inventra-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 10

  backend:
    image: eclipse-temurin:21-jre
    container_name: inventra-backend
    restart: unless-stopped
    working_dir: /app
    env_file:
      - .env
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
      SERVER_PORT: 8080
    volumes:
      - ./backend/inventra-backend.jar:/app/app.jar:ro
    command: ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - inventra-net

  nginx:
    image: nginx:1.27-alpine
    container_name: inventra-nginx
    restart: unless-stopped
    volumes:
      - ./web/dist:/usr/share/nginx/html:ro
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - inventra-net

volumes:
  postgres_data:

networks:
  inventra-net:
EOF
```

Validar configuración:

```bash
docker compose config
```

---

## 10. Levantar solo Postgres

```bash
cd /opt/inventra

docker compose up -d postgres
docker compose ps
```

Resultado esperado:

```text
inventra-postgres   postgres:16   Up   healthy   5432/tcp
```

---

## 11. Instalar Java, Maven y Node

Instalar Java 21 y Maven:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven git curl unzip
```

Instalar Node 20:

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

Validar:

```bash
java -version
mvn -version
node -v
npm -v
```

---

## 12. Preparar código fuente backend

El backend usa **Maven**, no Gradle.

Ubicación sugerida:

```text
/opt/inventra-source-code
```

Clonar backend:

```bash
cd /opt

git clone URL_DE_TU_BACKEND inventra-source-code
cd /opt/inventra-source-code
```

El root del repo debe tener el `pom.xml` padre.

---

## 13. Compilar backend Maven

Desde el root del repo:

```bash
cd /opt/inventra-source-code
```

Compilar módulo backend con dependencias:

```bash
mvn clean package -DskipTests \
  -pl inventra-backend -am \
  -Dmaven.compiler.release=21
```

Si aparece este error:

```text
Source option 5 is no longer supported. Use 8 or later.
Target option 5 is no longer supported. Use 8 or later.
```

usar esta variante:

```bash
mvn clean package -DskipTests \
  -pl inventra-backend -am \
  -Dmaven.compiler.source=21 \
  -Dmaven.compiler.target=21
```

---

## 14. Arreglar JAR no ejecutable

Error visto:

```text
no main manifest attribute, in /app/app.jar
```

Eso significa:

```text
Se generó un JAR normal, no un JAR ejecutable de Spring Boot.
```

En:

```text
inventra-backend/pom.xml
```

dentro de:

```xml
<build>
    <plugins>
        ...
    </plugins>
</build>
```

agregar o reemplazar el plugin de Spring Boot por este:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring.boot.version}</version>
    <configuration>
        <mainClass>mx.terabyte.labs.inventra.InventraBackendApplication</mainClass>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

No ponerlo solo en `pluginManagement`.

Recompilar:

```bash
cd /opt/inventra-source-code

mvn clean package -DskipTests \
  -pl inventra-backend -am \
  -Dmaven.compiler.release=21
```

Validar manifest:

```bash
unzip -p ./inventra-backend/target/inventra-backend-*.jar META-INF/MANIFEST.MF | head -50
```

Debe salir algo parecido a:

```text
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: mx.terabyte.labs.inventra.InventraBackendApplication
```

---

## 15. Copiar JAR al deploy

Listar JARs:

```bash
ls -lh /opt/inventra-source-code/inventra-backend/target/*.jar
```

Copiar el JAR que **NO** termina en `.original`:

```bash
cp /opt/inventra-source-code/inventra-backend/target/inventra-backend-1.0-SNAPSHOT.jar \
  /opt/inventra/backend/inventra-backend.jar
```

Validar el JAR copiado:

```bash
unzip -p /opt/inventra/backend/inventra-backend.jar META-INF/MANIFEST.MF | head -50
```

Debe salir:

```text
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: mx.terabyte.labs.inventra.InventraBackendApplication
```

---

## 16. Levantar backend

```bash
cd /opt/inventra

docker compose up -d backend
docker compose ps
docker logs -f inventra-backend --tail=200
```

Esperado:

```text
Started InventraBackendApplication
```

Flyway debe impactar la BD vacía al arrancar el backend:

```text
Postgres vacío
→ Backend arranca
→ Flyway ejecuta V1...V19
→ BD lista
```

---

## 17. Validar Flyway y tablas

Validar migraciones:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c \
"select installed_rank, version, description, success from flyway_schema_history order by installed_rank;"
```

Validar tablas de inventario:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "\dt inventory.*"
```

Validar tablas de manufactura:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "\dt manufacturing.*"
```

Validar tablas de seguridad:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "\dt security.*"
```

---

## 18. Levantar Nginx

```bash
cd /opt/inventra

docker compose up -d nginx
docker compose ps
```

Probar página:

```bash
curl -I http://localhost/
```

Probar proxy API:

```bash
curl -i http://localhost/api/v1/products
```

Respuesta esperada sin token:

```text
HTTP/1.1 401
```

Ejemplo válido:

```json
{
  "code": "UNAUTHORIZED",
  "message": "Authentication is required or token is invalid",
  "data": null,
  "details": [],
  "timestamp": "..."
}
```

Eso significa:

```text
Nginx ✅
proxy /api ✅
backend ✅
Spring Security ✅
JSON de error custom ✅
```

---

## 19. Nota importante sobre `localhost:8080`

Este comando falla y es normal:

```bash
curl -i http://localhost:8080/api/v1/products
```

Porque el backend no publica 8080 al host.

El backend vive dentro de la red Docker y Nginx le pega por:

```text
http://backend:8080
```

La prueba correcta desde el host es:

```bash
curl -i http://localhost/api/v1/products
```

---

## 20. Probar backend directo dentro de la red Docker

Ver redes:

```bash
docker network ls
```

Buscar red parecida:

```text
inventra_inventra-net
```

Probar con contenedor curl:

```bash
docker run --rm --network inventra_inventra-net curlimages/curl:latest \
  -i http://backend:8080/api/v1/products
```

Si la red tiene otro nombre, cambiar `inventra_inventra-net` por el nombre correcto.

---

## 21. Preparar frontend `inventra-web`

El frontend es otro proyecto aparte.

Ubicación sugerida:

```text
/opt/inventra-web
```

Clonar frontend:

```bash
cd /opt

git clone URL_DE_TU_FRONTEND inventra-web
cd /opt/inventra-web
```

Si ya existe y no recuerdas dónde está:

```bash
sudo find /opt -maxdepth 4 -name package.json -not -path "*/node_modules/*" -print
```

---

## 22. Configurar frontend para usar Nginx proxy

En el root de `inventra-web`:

```bash
cd /opt/inventra-web

cat > .env.production <<'EOF'
VITE_API_BASE_URL=/api/v1
EOF
```

Esto hace que React llame a:

```text
/api/v1/products
/api/v1/auth/login
```

y Nginx mande esas llamadas al backend.

---

## 23. Build frontend

Desde el root de `inventra-web`:

```bash
cd /opt/inventra-web

npm install
npm run build
```

Copiar build a Nginx:

```bash
rm -rf /opt/inventra/web/dist/*
cp -r dist/* /opt/inventra/web/dist/
```

Reiniciar Nginx:

```bash
cd /opt/inventra
docker compose restart nginx
```

Probar:

```bash
curl -I http://localhost/
```

Abrir navegador:

```text
http://TU_IP_PUBLICA
```

Debe salir el login real de Inventra.

---

## 24. Estado actual alcanzado

Hasta este punto ya quedó validado:

```text
Postgres ✅
Backend ✅
Nginx proxy ✅
/api/v1/products responde 401 ✅
```

Pendiente:

```text
Build y subir inventra-web
Validar login
Crear seed o usuario demo si la BD no tiene usuario
```

---

## 25. Comandos útiles

Ver estado:

```bash
cd /opt/inventra
docker compose ps
```

Ver logs backend:

```bash
docker logs -f inventra-backend --tail=200
```

Ver logs Nginx:

```bash
docker logs -f inventra-nginx --tail=200
```

Reiniciar backend:

```bash
docker compose restart backend
```

Reiniciar Nginx:

```bash
docker compose restart nginx
```

Bajar todo sin borrar datos:

```bash
docker compose stop
```

Levantar todo:

```bash
docker compose up -d
```

Borrar contenedores sin borrar volumen:

```bash
docker compose down
```

Borrar todo incluyendo BD:

```bash
docker compose down -v
```

Advertencia:

```text
docker compose down -v
```

borra el volumen:

```text
postgres_data
```

y con eso se va la BD.

---

## 26. Pausar por hoy

Para detener los contenedores:

```bash
cd /opt/inventra
docker compose stop
```

No borrar todavía:

```text
No delete instance
No detach static IP
No borrar /opt/inventra
```

---

## 27. Continuar en la siguiente sesión

Levantar todo:

```bash
cd /opt/inventra
docker compose up -d
docker compose ps
```

Probar API:

```bash
curl -i http://localhost/api/v1/products
```

Si responde `401`, el backend y Nginx están bien.

Luego seguir con frontend:

```bash
cd /opt/inventra-web

cat > .env.production <<'EOF'
VITE_API_BASE_URL=/api/v1
EOF

npm install
npm run build

rm -rf /opt/inventra/web/dist/*
cp -r dist/* /opt/inventra/web/dist/

cd /opt/inventra
docker compose restart nginx
```

Abrir:

```text
http://TU_IP_PUBLICA
```

---

## 28. Troubleshooting

### Error: `no main manifest attribute`

Causa:

```text
El JAR no es ejecutable de Spring Boot.
```

Solución:

```text
Agregar spring-boot-maven-plugin con goal repackage.
Recompilar.
Validar MANIFEST.MF.
Copiar el JAR que NO sea .original.
```

Validación:

```bash
unzip -p /opt/inventra/backend/inventra-backend.jar META-INF/MANIFEST.MF | head -50
```

Debe mostrar:

```text
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: mx.terabyte.labs.inventra.InventraBackendApplication
```

### Error: `Source option 5 is no longer supported`

Solución rápida:

```bash
mvn clean package -DskipTests \
  -pl inventra-backend -am \
  -Dmaven.compiler.source=21 \
  -Dmaven.compiler.target=21
```

Solución limpia en `pom.xml` padre:

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

### `curl localhost:8080` falla

Normal. El puerto 8080 no está publicado.

Probar así:

```bash
curl -i http://localhost/api/v1/products
```

### `/api/v1/products` responde 401

Eso está bien si no mandas token.

Significa:

```text
Backend vivo
Security viva
Proxy funcionando
```

### Backend no arranca

Revisar logs:

```bash
docker logs -f inventra-backend --tail=300
```

### Nginx no levanta

Revisar logs:

```bash
docker logs -f inventra-nginx --tail=200
```

### BD vacía

Flyway crea estructura.

Pero datos demo solo aparecen si las migraciones tienen inserts seed.

Si no hay usuario demo:

```text
Crear seed migration
o insertar usuario manual
o restaurar dump desde local
```
