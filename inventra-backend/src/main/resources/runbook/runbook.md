# Inventra Demo en AWS Lightsail

> Actualizado para incluir los cambios recientes de Inventra: fórmulas con ruta de fabricación, ejecución de órdenes paso por paso, proveedores con contactos, entradas/salidas de inventario, almacenes y validaciones nuevas.

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

---

## 1.1 Alcance funcional actual de Inventra

Este runbook asume que el código fuente ya contiene los cambios recientes del demo:

```text
Catálogos
  ├── Productos
  ├── Tipos de producto desde backend
  ├── Categorías
  └── Unidades de medida

Proveedores
  ├── Crear proveedor
  ├── Actualizar proveedor
  └── Agregar contactos

Inventario
  ├── Entradas / recepción de material
  ├── Salidas / despacho de inventario
  ├── Movimientos de auditoría
  ├── Existencias
  └── Almacenes

Manufactura
  ├── Fórmulas con versiones
  ├── Ruta/proceso de fabricación por fórmula
  ├── Insumos ligados a proceso
  ├── Órdenes de fabricación
  ├── Consumos
  ├── Producción
  ├── Movimientos de producción
  └── Ejecución paso por paso de la orden
```

Regla profesional del modelo:

```text
No se edita destructivamente una fórmula histórica.
Actualizar fórmula = crear nueva versión.
```

La fórmula define:

```text
Qué se produce
Cuánto se produce
Qué insumos consume
En qué paso se consume cada insumo
Qué ruta/proceso de fabricación debe seguirse
```

La orden de fabricación ejecuta:

```text
Una fórmula específica
Una versión específica
Una copia de los pasos de esa fórmula
Consumos reales
Producción real
Movimientos reales
```


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

## 12.1 Preflight obligatorio antes de compilar

Antes de generar el JAR o el build web, validar que los últimos cambios estén aplicados.

Desde el repo backend:

```bash
cd /opt/inventra-source-code

find . -path "*/db/migration/*" -name "V*.sql" | sort
```

Validar que existan migraciones para:

```text
formula_process_steps
formula_items.process_step_id
manufacturing_order_steps
```

Ejemplos de nombres esperados, ajustados al número real de Flyway:

```text
V13__formula_process_and_order_steps.sql
V14__manufacturing_order_steps_execution.sql
```

Importante:

```text
No dejar archivos con nombre V_NEXT__...
No repetir el mismo número de versión Flyway.
No renombrar migraciones ya aplicadas en una BD existente.
```

Si `manufacturing_order_steps` ya fue creado por una migración anterior con `CREATE TABLE IF NOT EXISTS`, una migración posterior idempotente no debe romper, pero el orden y numeración deben quedar claros.

Validar que el backend tenga estos componentes recientes:

```bash
find inventra-backend/src/main/java -name "*FormulaProcessStep*" -print
find inventra-backend/src/main/java -name "*ManufacturingOrderStep*" -print
```

Validar que el frontend tenga los tipos actualizados:

```bash
grep -R "processSteps" -n /opt/inventra-web/src/features || true
grep -R "ManufacturingOrderStep" -n /opt/inventra-web/src/features || true
```

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
→ Flyway ejecuta V1...hasta la última migración del repo
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

Validar tablas nuevas de proceso de fórmula y ejecución de órdenes:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "\d manufacturing.formula_process_steps"

docker exec -it inventra-postgres psql -U inventra -d inventra -c "\d manufacturing.manufacturing_order_steps"

docker exec -it inventra-postgres psql -U inventra -d inventra -c "\d manufacturing.formula_items"
```

Validar columnas clave:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "
select column_name, data_type
from information_schema.columns
where table_schema = 'manufacturing'
  and table_name = 'formula_items'
  and column_name = 'process_step_id';
"

docker exec -it inventra-postgres psql -U inventra -d inventra -c "
select column_name, data_type
from information_schema.columns
where table_schema = 'manufacturing'
  and table_name = 'manufacturing_order_steps'
order by ordinal_position;
"
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


---

## 18.1 Smoke tests de módulos recientes

Sin token, la mayoría de endpoints protegidos pueden responder `401`, y eso está bien porque valida proxy/backend/security.

Fórmulas:

```bash
curl -i "http://localhost/api/v1/manufacturing/formulas?page=0&size=5"
```

Detalle de fórmula con versión:

```bash
curl -i "http://localhost/api/v1/manufacturing/formulas/PAINT-WHITE/versions/1"
```

Si tienes token:

```bash
TOKEN="PEGA_AQUI_TU_TOKEN"

curl -s "http://localhost/api/v1/manufacturing/formulas?page=0&size=5"   -H "Authorization: Bearer ${TOKEN}" | jq
```

La respuesta de detalle debe incluir, si la fórmula ya fue creada con ruta:

```json
{
  "processStepsCount": 5,
  "processSteps": [],
  "items": []
}
```

Órdenes de fabricación paso por paso:

```bash
ORDER_NUMBER="MO-2026-0001"

curl -i "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps"
```

Con token:

```bash
curl -s "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps"   -H "Authorization: Bearer ${TOKEN}" | jq
```

Acciones de pasos:

```bash
curl -X POST "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps/1/start"   -H "Authorization: Bearer ${TOKEN}"   -H "Content-Type: application/json"   -d '{"notes":"Inicio de paso desde smoke test"}'

curl -X POST "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps/1/complete"   -H "Authorization: Bearer ${TOKEN}"   -H "Content-Type: application/json"   -d '{"notes":"Paso completado desde smoke test"}'
```

Para control de calidad:

```bash
curl -X POST "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps/3/pass"   -H "Authorization: Bearer ${TOKEN}"   -H "Content-Type: application/json"   -d '{"notes":"Control aprobado"}'

curl -X POST "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps/3/fail"   -H "Authorization: Bearer ${TOKEN}"   -H "Content-Type: application/json"   -d '{"notes":"Control rechazado"}'
```

Regla esperada:

```text
La orden no debe completarse si tiene pasos pendientes, en proceso o fallidos.
```


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


Validar específicamente que el build incluya las pantallas recientes:

```bash
grep -R "Ruta de fabricación" -n src || true
grep -R "processSteps" -n src || true
grep -R "manufacturing_order_steps" -n src || true
grep -R "steps/" -n src/features || true
```

Si `npm run build` falla con errores tipo:

```text
Property 'processSteps' does not exist on type 'FormulaDetail'
Property 'processStepNumber' does not exist on type 'FormulaItem'
Property 'processStepsCount' does not exist on type 'FormulaSearch'
```

entonces el frontend tiene `FormulasPage.tsx` nuevo pero `formulaTypes.ts` viejo.

Solución:

```text
Actualizar src/features/formulas/formulaTypes.ts
o actualizar la ruta equivalente si el módulo vive en src/features/manufacturing
```

Si la pantalla se ve descuadrada, inputs oscuros o modal al final de la página:

```text
Revisar que el fix CSS de FormulasPage.css esté pegado al final del archivo.
Después hacer hard refresh: Ctrl + Shift + R.
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
Validar fórmulas con procesos
Validar órdenes de fabricación paso por paso
```

---

## 24.1 Estado funcional esperado después de los últimos cambios

Con el código actual, además de login/productos, se espera poder validar:

```text
Productos
  ├── Crear / actualizar
  ├── Tipo de producto mostrado desde backend
  └── Activar / desactivar

Proveedores
  ├── Crear / actualizar
  └── Agregar contactos

Inventario
  ├── Registrar entrada
  ├── Registrar salida
  ├── Ver movimientos
  └── Ver existencias

Fórmulas
  ├── Crear fórmula
  ├── Crear nueva versión
  ├── Definir ruta de fabricación
  ├── Ligar insumos a procesos
  └── Ver detalle con procesos

Órdenes de fabricación
  ├── Crear orden
  ├── Iniciar orden
  ├── Ejecutar proceso paso por paso
  ├── Registrar consumos
  ├── Registrar producción
  ├── Ver movimientos
  └── Completar solo cuando los pasos estén cerrados correctamente
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


### Error: tabla `manufacturing_order_steps` no existe

Causa probable:

```text
No se aplicó la migración de ejecución de pasos.
```

Validar:

```bash
docker exec -it inventra-postgres psql -U inventra -d inventra -c "\dt manufacturing.manufacturing_order_steps"
```

Solución:

```text
Revisar migraciones Flyway.
Renombrar V_NEXT__... al número real.
Recompilar backend.
Recrear BD solo si es demo y puedes perder datos.
```

### Error: `processSteps` no existe en frontend

Causa probable:

```text
FormulasPage.tsx nuevo + formulaTypes.ts viejo.
```

Solución:

```text
Actualizar formulaTypes.ts con:
- FormulaProcessStep
- FormulaSearch.processStepsCount
- FormulaDetail.processStepsCount
- FormulaDetail.processSteps
- FormulaItem.processStepNumber
- FormulaItem.processStepName
- CreateFormulaRequest.processSteps
```

### Pantalla Fórmulas descuadrada

Síntomas:

```text
Botón Nueva fórmula gigante
Íconos enormes
Inputs oscuros
Modal aparece abajo de la página
```

Solución:

```text
Pegar el fix CSS al final de FormulasPage.css.
Reiniciar Vite si aplica.
Hard refresh en navegador.
```

### No deja completar orden

Ahora es esperado si faltan pasos.

Validar pasos:

```bash
curl -s "http://localhost/api/v1/manufacturing/orders/${ORDER_NUMBER}/steps"   -H "Authorization: Bearer ${TOKEN}" | jq
```

Estados que bloquean completar:

```text
PENDING
IN_PROGRESS
FAILED
```

Cerrar pasos pendientes, aprobar calidad o corregir el proceso antes de completar.


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