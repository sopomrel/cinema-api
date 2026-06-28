# Cinema API — Spring Boot Final Project

Production-oriented REST API for managing movies, actors, and directors. Built on the midterm application and extended with Spring Security, externalized configuration, internationalization, structured logging, automated tests, and Spring Boot Actuator monitoring.

## Project description

The Cinema API exposes CRUD endpoints for a movie catalog with relationships between movies, actors, and directors. The application follows a layered architecture (Controller → Service → Repository), uses DTOs for API contracts, validates input with Bean Validation, and returns consistent error responses via a global exception handler.

**Core domain:** movies (title, year, genre, rating, director, cast), actors, and directors.

**Key capabilities:**

- REST API with OpenAPI / Swagger documentation
- JPA persistence (H2 in development, PostgreSQL in production)
- Role-based authentication and authorization (USER, ADMIN)
- Georgian and English message bundles (`Accept-Language`)
- Profile-specific configuration (`dev`, `prod`)
- Custom health indicator, info contributor, and Micrometer metrics
- Automated unit, integration, controller, and repository tests with JaCoCo coverage

---

## Technologies used

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Web | Spring Web MVC, Springdoc OpenAPI 2.8.8 |
| Persistence | Spring Data JPA, Hibernate, H2, PostgreSQL |
| Security | Spring Security (form login, HTTP Basic, `@PreAuthorize`) |
| Validation | Jakarta Bean Validation |
| Monitoring | Spring Boot Actuator, Micrometer |
| Logging | SLF4J, Logback (`logback-spring.xml`) |
| Testing | JUnit 5, Mockito, MockMvc, `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest` |
| Coverage | JaCoCo Maven Plugin 0.8.12 |
| Build | Maven |

---

## Run the application

### Prerequisites

- JDK 21
- Maven 3.9+
- PostgreSQL (only for `prod` profile)

### Development profile (default)

```bash
mvn spring-boot:run
```

Or explicitly:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**IntelliJ IDEA:** Run `CinemaApplication` → Edit Configurations → Active profiles: `dev`

### Production profile

Requires a running PostgreSQL instance:

```bash
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=cinemadb
set DB_USERNAME=cinema_user
set DB_PASSWORD=cinema_pass
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**IntelliJ IDEA:** Active profiles: `prod`

### Useful URLs

| Resource | URL |
|----------|-----|
| API root | `http://localhost:8080/` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| H2 console (dev only) | `http://localhost:8080/h2-console` |

H2 JDBC URL: `jdbc:h2:mem:cinemadb`, user: `sa`, no password.

---

## Profile configuration

| Profile | Database | DDL | H2 Console | Sample data | Catalog GET public | Health details | Log level (`com.cinema`) |
|---------|----------|-----|------------|-------------|-------------------|----------------|--------------------------|
| `dev` | H2 in-memory | `create-drop` | Enabled | Seeded (Nolan, DiCaprio, Inception) | Yes | Always shown | `DEBUG` |
| `prod` | PostgreSQL | `validate` | Disabled | None | No (auth required) | When authorized | `WARN` |

Configuration files:

- `application.yml` — shared defaults, Actuator exposure, `info.app.*` metadata
- `application-dev.yml` — H2, dev `app.settings`, `management.endpoint.health.show-details: always`
- `application-prod.yml` — PostgreSQL datasource from env vars, stricter catalog access

Custom properties (`AppSettings.java`, prefix `app.settings`):

| Property | Role |
|----------|------|
| `app.settings.title` | Application title in `GET /` and Actuator info |
| `app.settings.contact-email` | Support email (validated with `@Email`) |
| `app.settings.pagination-limit` | Max movies returned by `GET /api/movies` (1–100) |
| `app.settings.external-service-url` | External movie DB URL in metadata |
| `app.settings.catalog-public-enabled` | When `false`, catalog GET endpoints require authentication |

---

## User credentials and roles

Users are stored in the database (`app_users` table). Passwords are hashed with BCrypt at startup.

| Username | Password | Roles |
|----------|----------|-------|
| `user` | `user123` | `ROLE_USER` |
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_USER` |

| Role | Description |
|------|-------------|
| **USER** | Create/update movies and actors; view profile (`/api/auth/me`). |
| **ADMIN** | Full control: delete entities, manage directors, admin dashboard, Actuator endpoints (except public health). Inherits USER capabilities. |

### Authentication

**Form login (session cookie):**

```http
POST /api/auth/login
Content-Type: application/x-www-form-urlencoded

username=user&password=user123
```

**Logout:** `POST /api/auth/logout` → `204 No Content`

**HTTP Basic:** Use `Authorization: Basic …` in Postman or Swagger **Authorize** dialog.

---

## Testing instructions

### Run all tests

```bash
mvn test
```

### Generate JaCoCo coverage report

```bash
mvn test jacoco:report
```

Open `target/site/jacoco/index.html` in a browser.

### Test suite overview

| Test class | Type | What it covers |
|------------|------|----------------|
| `ActorServiceImplTest` | Unit (Mockito) | Service logic, parameterized scenarios, positive/negative cases |
| `MovieServiceImplTest` | Unit (Mockito) | Movie creation, pagination, Micrometer counter |
| `DirectorServiceImplTest` | Unit (Mockito) | Director CRUD, parameterized not-found, duplicate-email cases |
| `GlobalExceptionHandlerTest` | Unit (Mockito) | Localized error responses |
| `ActorControllerTest` | `@WebMvcTest` + MockMvc | Controller layer, auth, validation |
| `ActorRepositoryTest` | `@DataJpaTest` | JPA queries, email uniqueness |
| `SecurityIntegrationTest` | Integration (MockMvc) | End-to-end security rules |
| `ActuatorIntegrationTest` | Integration (MockMvc) | Health, info, metrics endpoints and security |
| `ApiEndToEndTest` | Integration (`TestRestTemplate`, `RANDOM_PORT`) | Full HTTP black-box: public reads, auth, role-based access |
| `CinemaApplicationTests` | Context load | Application starts with `dev` profile |

Tests use `src/test/resources/application.yml` with the `dev` profile.

### Code coverage gate

JaCoCo enforces a **minimum 60% line coverage** (bundle level) during the `test`
phase via the `coverage-check` execution. The build fails if coverage drops below
the threshold, and an HTML report is always written to `target/site/jacoco/index.html`.

---

## Monitoring endpoints

Spring Boot Actuator is enabled with the following exposed endpoints:

| Endpoint | Access | Description |
|----------|--------|-------------|
| `GET /actuator/health` | Public | Application health (includes DB status) |
| `GET /actuator/info` | ADMIN | Build and custom app metadata |
| `GET /actuator/metrics` | ADMIN | Available Micrometer metrics |
| `GET /actuator/metrics/{name}` | ADMIN | Single metric (e.g. `cinema.movies.created`) |

### Security

Configured in `SecurityConfig`:

- `/actuator/health` — public (liveness probe friendly)
- `/actuator/**` (info, metrics, etc.) — `ROLE_ADMIN` required

### Health details

- **dev:** `show-details: always` — full component breakdown including custom `cinema` indicator
- **prod:** `show-details: when_authorized` — details visible only to authenticated users

### Custom components

| Component | Class | Purpose |
|-----------|-------|---------|
| Custom health indicator | `CinemaHealthIndicator` | Reports `moviesInCatalog` count |
| Info contributor | `CinemaInfoContributor` | Exposes `app.settings` in `/actuator/info` |
| Custom metric | `MovieServiceImpl` | Counter `cinema.movies.created` on each movie creation |

### Example requests

```bash
# Public health check
curl http://localhost:8080/actuator/health

# Admin-only info (HTTP Basic)
curl -u admin:admin123 http://localhost:8080/actuator/info

# Admin-only metrics
curl -u admin:admin123 http://localhost:8080/actuator/metrics
```

---

## Logging configuration

**Framework:** SLF4J with Logback (`logback-spring.xml`).

| Feature | Configuration |
|---------|---------------|
| Console logging | Spring Boot default console appender |
| File logging | `logs/app.log` |
| Rolling policy | Daily rotation to `logs/app.yyyy-MM-dd.log`, 30-day retention, 100 MB total cap |
| Profile levels | `com.cinema` → `DEBUG` (dev), `WARN` (prod) |
| Parameterized logging | `@Slf4j` with `{}` placeholders in services and controllers |

**Components with structured logging:** `HomeController`, `AuthController`, `MovieServiceImpl`, `ActorServiceImpl`, `DirectorServiceImpl`, `GlobalExceptionHandler`, `UserDataInitializer`, `DevDataInitializer`, `CustomUserDetailsService`.

| Level | Usage |
|-------|-------|
| `DEBUG` | User lookup, pagination (dev) |
| `INFO` | Resource create/delete, home/profile access, data seeding |
| `WARN` | Not-found, duplicate-resource, access denied |
| `ERROR` | Unexpected exceptions in `GlobalExceptionHandler` |

---

## Internationalization (i18n)

**Bundles:** `messages.properties` (default), `messages_en.properties`, `messages_ka.properties` (UTF-8).

**Locale:** `AcceptHeaderLocaleResolver` — send `Accept-Language: en` or `Accept-Language: ka`.

Localized areas: welcome message, auth login, validation errors, global exception handler, security access denied.

---

## Endpoint access rules

### Public (no login)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Welcome metadata |
| GET | `/actuator/health` | Health check |
| GET | `/api/movies/**`, `/api/actors/**`, `/api/directors/**` | Catalog browse (dev; prod requires auth when `catalog-public-enabled: false`) |
| POST | `/api/auth/login` | Login |
| GET | `/swagger-ui/**`, `/api-docs/**` | API documentation |
| GET | `/h2-console/**` | H2 console (dev only) |

### Authenticated (USER or ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/me` | Current user profile |
| POST/PUT | `/api/movies`, `/api/actors` | Create/update movies and actors |

### ADMIN only

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/summary` | System statistics |
| GET | `/actuator/info`, `/actuator/metrics/**` | Monitoring |
| POST/PUT/DELETE | `/api/directors/**` | Manage directors |
| DELETE | `/api/movies/{id}`, `/api/actors/{id}` | Delete entities |

Method-level security (`@PreAuthorize`) is applied on service implementations and admin/auth controllers.

---

## CSRF

CSRF protection is **disabled** for this REST API (Postman, Swagger, HTTP Basic). Re-enable in `SecurityConfig` if server-rendered HTML forms are added.

---

## Quick test scenarios

Use Swagger (`/swagger-ui.html`) → **Authorize** with the credentials below.

> A movie requires an existing director (`directorId`). Actors are optional (`actorIds`).
>
> In **dev**, sample data (Nolan, DiCaprio, Inception) is seeded automatically.

1. **Public read:** `GET /api/movies` → `200` without credentials.
2. **Unauthenticated create:** `POST /api/movies` without login → `401`.
3. **USER creates movie:** Authorize as `user` / `user123`, `POST /api/movies` with valid `directorId` → `201`.
4. **ADMIN delete:** Authorize as `admin` / `admin123`, `DELETE /api/movies/1` → `204`.
5. **USER denied delete:** Authorize as `user`, `DELETE /api/movies/1` → `403`.
6. **Admin dashboard:** `GET /api/admin/summary` as admin → `200`; as user → `403`.
7. **Actuator:** `GET /actuator/health` → `200` (public); `GET /actuator/info` as admin → `200`.

---

## Project structure

```
src/main/java/com/cinema/
├── config/          Security, Swagger, locale, validation, data initializers
├── controller/      REST controllers (movies, actors, directors, auth, admin)
├── service/         Service interfaces
│   └── impl/        Service implementations
├── repository/      Spring Data JPA repositories
├── entity/          JPA entities
├── dto/             Request/response DTOs
├── mapper/          Entity ↔ DTO mappers
├── exception/       Custom exceptions, GlobalExceptionHandler, ErrorResponse
├── security/        CustomUserDetailsService, RoleType
└── health/          CinemaHealthIndicator, CinemaInfoContributor
```
