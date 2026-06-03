# Spendly Backend

Spring Boot backend for Spendly. This service will replace the legacy Supabase
auth and persistence flow.

## Stack

- Java 21 LTS
- Spring Boot 3.5
- Groovy Gradle
- PostgreSQL
- Liquibase
- Railway

## Package Layout

The Java package layout follows a role-oriented component style:

```text
src/main/java/com/spendly/
  SpendlyBackendApplication.java
  client/          External HTTP/API clients
  config/          Spring configuration and properties
  constant/        Enums and constants
  controller/      REST controllers
  dto/
    model/         Shared API models
    request/       Request DTOs
    response/      Response DTOs
  entity/          Database entities
  exception/       Domain errors and API exception handling
  mapper/          DTO/entity mappers
  orchestrator/    Use-case orchestration
  repository/      Database repositories
  service/         Service interfaces
  service/impl/    Service implementations
  tool/            Internal utilities/tools
```

Entities should extend `AuditSection` for ULID ids and audit fields:

```text
id
created_at
created_by
last_modified_at
last_modified_by
```

User-facing auth responses expose `refNo`, not email. Email remains stored for
sign-in and OTP delivery only.

## Liquibase Layout

Liquibase follows the same component-style resource layout:

```text
src/main/resources/liquibase/
  master.xml       Spring Boot Liquibase entry point
  component.xml    Ordered include list for this service
  changelog/       Versioned schema changelogs
  data/            CSV seed/reference data, when needed
  sql/             Raw SQL scripts, when needed
```

Add new changelogs under `liquibase/changelog/` and include them in
`liquibase/component.xml` in execution order.

## Local Run

Start PostgreSQL locally with a `spendly` database and matching credentials, or
override the datasource variables.

```bash
./gradlew bootRun
```

Health check:

```bash
curl http://localhost:8080/api/health
```

## Test

Tests use H2 with PostgreSQL compatibility mode.

```bash
./gradlew test
```

## Format

Java formatting is handled by Spring Java Format.

```bash
./gradlew format
./gradlew checkFormat
```

## Build

```bash
./gradlew bootJar
java -jar build/libs/spendly-backend.jar
```

## Docker

Build the backend image:

```bash
docker build -t spendly-backend .
```

Run the backend with local Postgres and Mailpit:

```bash
docker compose up --build
```

Useful local URLs:

```text
Backend: http://localhost:8080
Health:  http://localhost:8080/actuator/health
Mailpit: http://localhost:8025
```

The compose file starts:

```text
postgres  PostgreSQL database using spendly/spendly
mailpit   Local SMTP inbox for OTP emails
backend   Spring Boot API
```

Stop services:

```bash
docker compose down
```

Remove the local Postgres volume:

```bash
docker compose down -v
```

## Railway

Set the Railway service root to:

```text
spendly-backend
```

Add a Railway PostgreSQL database, then configure the backend service variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
APP_CORS_ALLOWED_ORIGINS=https://spendly-tawny-two.vercel.app
APP_FRONTEND_URL=https://spendly-tawny-two.vercel.app
APP_MAIL_FROM=no-reply@spendly.com
JWT_SECRET=<long-random-secret-at-least-32-bytes>
SPRING_MAIL_HOST=<smtp-host>
SPRING_MAIL_PORT=<smtp-port>
SPRING_MAIL_USERNAME=<smtp-username>
SPRING_MAIL_PASSWORD=<smtp-password>
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

Railway should run:

```bash
java -jar build/libs/spendly-backend.jar
```

`railway.toml` includes this start command and `/actuator/health` health check.

## Auth API

OTP-only auth replaces Supabase magic links.

```http
POST /api/auth/send-otp
POST /api/auth/verify-otp
POST /api/auth/refresh
GET  /api/auth/me
```

OTP standard:

```text
6 numeric digits
10 minute expiry
one-time use
hashed at rest
2 send requests per email+IP per 60 seconds
5 verify attempts per email+IP per 10 minutes
1 hour access token
30 day rotating refresh token
```

## Spendly API

All routes below require an `Authorization: Bearer <accessToken>` header.

```http
GET    /api/profile
PUT    /api/profile
GET    /api/purchase-checks
POST   /api/purchase-checks
DELETE /api/me/data
```

Profile request:

```json
{
  "monthlyIncome": 5000,
  "monthlyCommitments": 2200,
  "currentBalance": 1800.5,
  "protectedBuffer": 300,
  "lastBalanceUpdate": "2026-06-03"
}
```

Purchase check request:

```json
{
  "amount": 42.9,
  "verdict": "Safe",
  "consequence": "This keeps you on track.",
  "checkedAt": "2026-06-03T04:30:00Z"
}
```

`GET /api/purchase-checks` returns the latest 8 checks, newest first. Valid
verdict values are `Safe`, `Risky`, and `Not safe`.

## Supabase Sunset Plan

1. Implement replacement auth APIs. Done.
2. Implement profile APIs. Done.
3. Implement purchase check history APIs. Done.
4. Point `spendly-frontend` to this backend.
5. Remove Supabase routes, packages, and schema files from the frontend.
