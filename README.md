# Inventra

Monorepo for the Inventra project — a modular Java Spring Boot application. This repository is organized as a multi-module Maven project with three main modules:

- `inventra-backend` — Spring Boot application (REST API, web layer, services, controllers).
- `inventra-bd` — database-related code and Flyway migrations.
- `inventra-common` — shared code (DTOs, utilities, enums, domain classes).

This README explains how to build, run and develop the project locally.

---

## Table of Contents

- Project overview
- Prerequisites
- Build
- Run (local)
- Database and migrations
- Configuration
- Tests
- Common developer tasks
- Troubleshooting
- Contributing
- License

---

## Project overview

This is a multi-module Maven project (parent `pom.xml`) that aggregates the modules listed above. The Spring Boot application lives in `inventra-backend` and depends on `inventra-common` and `inventra-bd`.

Key conventions used in the project:

- Java 21 (see parent `pom.xml` property `maven.compiler.release`).
- Spring Boot (version declared in parent `pom.xml`).
- Flyway migrations are used for database schema/versioning (see `inventra-bd/src/main/resources/db/migration`).

---

## Prerequisites

- JDK 21 installed and JAVA_HOME set
- Maven 3.8+ installed
- PostgreSQL (or a compatible DB) for development
- Recommended: IntelliJ IDEA (the project was developed in IntelliJ)

You can check your Java and Maven versions with:

```zsh
java -version
mvn -v
```

---

## Build

Build the whole multi-module project:

```zsh
mvn clean package
```

Build and run tests (default):

```zsh
mvn clean test
```

If you only want to build the backend module (and its dependencies):

```zsh
mvn -pl inventra-backend -am clean package
```

Notes:

- `-pl` selects a module; `-am` builds required modules as well.
- Use `-DskipTests` to skip tests during packaging when needed.

---

## Run (local)

The Spring Boot application is in `inventra-backend`. You can run it from the project root using Maven:

```zsh
# from the project root
mvn -pl inventra-backend spring-boot:run -f pom.xml
```

Or run the packaged jar (after `mvn package`):

```zsh
java -jar inventra-backend/target/inventra-backend-1.0-SNAPSHOT.jar
```

Default server port is `8080` (see `inventra-backend/src/main/resources/application.yml`). API base path examples:

- GET /api/v1/inventory/movements — list inventory movements (used in development/testing)

---

## Database and migrations

The project uses Flyway for database migrations. Migration scripts are located in the `inventra-bd` module under:

```
inventra-bd/src/main/resources/db/migration
```

By default, `inventra-backend/src/main/resources/application.yml` points to a PostgreSQL instance on `jdbc:postgresql://localhost:5432/inventra` with username/password `inventra`/`inventra`.

You can override these values using environment variables (Spring Boot supports `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`) or via command-line properties:

```zsh
mvn -pl inventra-backend spring-boot:run -Dspring.datasource.url=jdbc:postgresql://localhost:5432/inventra -Dspring.datasource.username=foo -Dspring.datasource.password=bar
```

When the application starts, Flyway will run migrations (if enabled). If you need to run Flyway separately, you may use the Flyway CLI or configure an execution via Maven.

---

## Configuration

Main application configuration for local development:

File: `inventra-backend/src/main/resources/application.yml`

Important keys you might change:

- spring.datasource.url
- spring.datasource.username
- spring.datasource.password
- server.port

For production deployments, prefer externalizing configuration via environment variables, a secrets manager, or Spring profiles.

---

## Tests

Run all tests:

```zsh
mvn test
```

Run tests for a particular module (example: backend):

```zsh
mvn -pl inventra-backend test
```

Unit and integration tests should live inside the respective module's `src/test/java` directory.

---

## Common developer tasks

- Import project in IntelliJ: Open the parent `pom.xml` as a Maven project. IntelliJ will import modules automatically.
- To run the backend from IntelliJ, use the Maven tool window or create a Spring Boot run configuration pointing to `inventra-backend`.
- To inspect SQL migrations: look under `inventra-bd/src/main/resources/db/migration` (Flyway versioned scripts: `V1__...`, `V2__...`, ...).

---

## Troubleshooting

- No plugin found for prefix 'spring-boot': ensure the `spring-boot-maven-plugin` is declared in `inventra-backend/pom.xml` (this project includes it in the module pom).
- Database connection errors: check `spring.datasource.*` properties, verify the DB is running and reachable, and that the user has the right privileges.
- LazyInitializationException when accessing lazy associations: ensure the code accesses lazy associations inside a transactional boundary (e.g. annotate service read methods with `@Transactional(readOnly = true)`) or fetch associations eagerly/with JOIN FETCH in queries when needed.

If you encounter a stack trace, search for the exception class name and the source file in the trace to locate the failing code path.

---

## Contributing

1. Fork the repository and create a feature branch.
2. Follow the project style (use existing code patterns).
3. Add tests for new features or bug fixes.
4. Open a pull request describing your changes.

Please run the test suite and ensure Flyway migrations are valid before creating a PR.

---

## License

This repository does not include a license file. Add a `LICENSE` to clarify terms if you plan to make the project public.

---

If you'd like, I can also add a simple `CONTRIBUTING.md`, set up a GitHub Actions workflow for CI, or create a Docker Compose file for a local PostgreSQL instance. Tell me which you'd prefer next.

