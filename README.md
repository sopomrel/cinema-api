# Cinema API — Spring Boot Midterm + Security + Advanced Config

REST API for managing movies, actors, and directors, secured with Spring Security, externalized configuration, i18n, and structured logging.

## Run the application

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

Requires a running PostgreSQL instance. Set environment variables as needed:

```bash
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=cinemadb
set DB_USERNAME=cinema_user
set DB_PASSWORD=cinema_pass
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**IntelliJ IDEA:** Active profiles: `prod`

| Profile | Database | DDL | H2 Console | Sample data | Log level (`com.cinema`) |
|---------|----------|-----|------------|-------------|--------------------------|
| `dev` | H2 in-memory | `create-drop` | Enabled | Seeded automatically | `DEBUG` |
| `prod` | PostgreSQL | `validate` | Disabled | None | `WARN` |

- API root: `http://localhost:8080/` — welcome JSON with metadata and links
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console (dev only): `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cinemadb`, user: `sa`, no password)

---

## Custom configuration properties (`app.settings`)

Defined in `AppSettings.java` (`@ConfigurationProperties` + `@Validated`):

| Property | Role |
|----------|------|
| `app.settings.title` | Application title shown in `GET /` metadata |
| `app.settings.contact-email` | Support contact email in metadata (validated with `@Email`) |
| `app.settings.pagination-limit` | Max movies returned by `GET /api/movies` (1–100) |
| `app.settings.external-service-url` | External movie DB API URL in metadata |
| `app.settings.catalog-public-enabled` | Feature flag: when `true` (dev), `GET /api/movies|actors|directors` are public; when `false` (prod), they require authentication |

Injected into `HomeController` (metadata), `MovieServiceImpl` (pagination limit), and `SecurityConfig` (catalog access rules).

Profile overrides: `application-dev.yml` and `application-prod.yml`.

---

## Internationalization (i18n)

**Resource bundles:** `messages.properties` (default/fallback), `messages_en.properties` (English), `messages_ka.properties` (Georgian), UTF-8 encoded.

**Locale resolution:** `AcceptHeaderLocaleResolver` reads the `Accept-Language` header (`en` or `ka`).

### Localized endpoints and messages

| Area | Details |
|------|---------|
| `GET /` | Welcome message and status (`app.welcome`, `app.status.running`) |
| `POST /api/auth/login` | Success/failure messages (`auth.login.success`, `auth.login.failure`) |
| `GlobalExceptionHandler` | 404, 409, 400 validation summary, 500, access denied |
| `SecurityConfig` | Localized access-denied response (`error.access.denied`) |
| Validation DTOs | `ActorRequest`, `DirectorRequest`, `MovieRequest` use `{validation.*}` message keys (`ValidationConfig` wires `MessageSource` to Bean Validation) |

### Test i18n

```http
GET http://localhost:8080/
Accept-Language: en
```

```http
GET http://localhost:8080/api/movies/999
Accept-Language: ka
```

```http
POST http://localhost:8080/api/actors
Accept-Language: en
Content-Type: application/json

{"firstName": "A", "lastName": "B", "email": "bad"}
```

English returns `"First name must be between 2 and 50 characters"`; Georgian returns the equivalent in `messages.properties`.

---

## Logging

**Framework:** SLF4J via Lombok `@Slf4j` in `HomeController`, `AuthController`, `MovieServiceImpl`, `ActorServiceImpl`, `DirectorServiceImpl`, `GlobalExceptionHandler`, `UserDataInitializer`, `DevDataInitializer`, `CustomUserDetailsService`.

| Level | Usage |
|-------|-------|
| `DEBUG` | User lookup, pagination, validation details (dev profile) |
| `INFO` | Home access, profile access, resource create/delete, user seeding |
| `WARN` | Not-found and duplicate-resource exceptions |
| `ERROR` | Unexpected exceptions in `GlobalExceptionHandler` |

**Log file location:** `logs/app.log` (project root, relative to working directory)

**Rotation:** Daily files at `logs/app.yyyy-MM-dd.log`, 30-day retention, 100 MB total cap (`logback-spring.xml`).

**Profile-driven levels:** `DEBUG` for `com.cinema` in `dev`, `WARN` in `prod`.

---

## Login credentials

Users are stored in the **database** (`app_users` table). Passwords are hashed with **BCrypt** at startup.


| Username | Password   | Roles                     |
| -------- | ---------- | ------------------------- |
| `user`   | `user123`  | `ROLE_USER`               |
| `admin`  | `admin123` | `ROLE_ADMIN`, `ROLE_USER` |


---

## User roles


| Role      | Description                                                                                            |
| --------- | ------------------------------------------------------------------------------------------------------ |
| **USER**  | Can create/update movies and actors; can view profile (`/api/auth/me`).                                |
| **ADMIN** | Full control: delete any entity, manage directors, access admin dashboard. Inherits USER capabilities. |


---

## Authentication (login / logout)

### Form login (session cookie)

```http
POST /api/auth/login
Content-Type: application/x-www-form-urlencoded

username=user&password=user123
```

Success: `200` + JSON `{"message":"Login successful","username":"user"}` and session cookie.

### Logout

```http
POST /api/auth/logout
```

Success: `204 No Content` (session invalidated).

### HTTP Basic (Postman / Swagger)

Send `Authorization: Basic …` with the same credentials. Click **Authorize** in Swagger UI and enter username/password.

---

## Endpoint access rules

### Public (no login)


| Method | Endpoint                         | Description         |
| ------ | -------------------------------- | ------------------- |
| GET    | `/api/movies/**`                 | Browse movies       |
| GET    | `/api/actors/**`                 | Browse actors       |
| GET    | `/api/directors/**`              | Browse directors    |
| POST   | `/api/auth/login`                | Login               |
| GET    | `/swagger-ui/**`, `/api-docs/**` | API documentation   |
| GET    | `/h2-console/**`                 | H2 database console |


### Authenticated (USER or ADMIN)


| Method | Endpoint           | Description                    |
| ------ | ------------------ | ------------------------------ |
| GET    | `/api/auth/me`     | Current user profile and roles |
| POST   | `/api/movies`      | Create movie                   |
| PUT    | `/api/movies/{id}` | Update movie                   |
| POST   | `/api/actors`      | Create actor                   |
| PUT    | `/api/actors/{id}` | Update actor                   |


### ADMIN only


| Method          | Endpoint             | Description                |
| --------------- | -------------------- | -------------------------- |
| GET             | `/api/admin/summary` | System statistics (counts) |
| POST/PUT/DELETE | `/api/directors/**`  | Manage directors           |
| DELETE          | `/api/movies/{id}`   | Delete movie               |
| DELETE          | `/api/actors/{id}`   | Delete actor               |


---

## Method-level security (`@PreAuthorize`)

Enabled via `@EnableMethodSecurity` in `SecurityConfig`.


| Class                 | Method                                               | Annotation                           |
| --------------------- | ---------------------------------------------------- | ------------------------------------ |
| `MovieServiceImpl`    | `createMovie`, `updateMovie`                         | `@PreAuthorize("isAuthenticated()")` |
| `MovieServiceImpl`    | `deleteMovie`                                        | `@PreAuthorize("hasRole('ADMIN')")`  |
| `DirectorServiceImpl` | `createDirector`, `updateDirector`, `deleteDirector` | `@PreAuthorize("hasRole('ADMIN')")`  |
| `AdminController`     | `getSummary`                                         | `@PreAuthorize("hasRole('ADMIN')")`  |
| `AuthController`      | `getCurrentUser`                                     | `@PreAuthorize("isAuthenticated()")` |


---

## CSRF

**CSRF protection is disabled** in `SecurityConfig`.

**Reason:** This project is a **REST API** used with Postman, Swagger, and HTTP Basic/session cookies—not server-rendered HTML forms. Spring Security’s CSRF token is designed for browser form posts. Disabling CSRF is standard for pure REST clients. If you add Thymeleaf/JSP forms later, re-enable CSRF in `SecurityConfig`.

---

## Security components


| Component                  | Location                                                                   |
| -------------------------- | -------------------------------------------------------------------------- |
| `SecurityConfig`           | `config/SecurityConfig.java` — `SecurityFilterChain`, BCrypt, login/logout |
| `CustomUserDetailsService` | `security/CustomUserDetailsService.java`                                   |
| `AppUser` entity           | `entity/AppUser.java` — DB users with encrypted passwords                  |
| `UserDataInitializer`      | `config/UserDataInitializer.java` — seeds default users                    |


---

## Quick test scenarios

Use **Swagger** (`/swagger-ui.html`) → **Authorize** with the credentials shown in each step.

> **Important:** A movie requires an existing **director** (`directorId` is mandatory). **Actors** are optional (`actorIds`).
>
> In the **`dev` profile**, sample data (director, actor, movie "Inception") is seeded automatically — you can skip Setup A/B and jump to security checks. For a clean database or `prod`, create directors and actors first.

### Setup (do this first — optional in dev)

**Step A — Create a director (ADMIN only)**  
Authorize as `admin` / `admin123`, then `POST /api/directors`:

```json
{
  "firstName": "Christopher",
  "lastName": "Nolan",
  "email": "nolan@example.com",
  "nationality": "British"
}
```

Note the returned `id` (usually `1` on a fresh database). You need this as `directorId` below.

**Step B — Create an actor (USER or ADMIN)**  
Authorize as `user` / `user123`, then `POST /api/actors`:

```json
{
  "firstName": "Leonardo",
  "lastName": "DiCaprio",
  "email": "leo@example.com",
  "nationality": "American"
}
```

Note the returned `id` (usually `1`). Optional for movies, but useful for testing `actorIds`.

### Security checks

1. **Public read:** `GET /api/movies` → `200` without credentials.
2. **Unauthenticated create:** `POST /api/movies` without login → `401`.
3. **USER creates movie:** Authorize as `user` / `user123`, then `POST /api/movies` with a valid `directorId`:

```json
{
  "title": "Inception",
  "releaseYear": 2010,
  "genre": "Sci-Fi",
  "rating": 8.8,
  "directorId": 1,
  "actorIds": [1]
}
```

→ `201` (omit `actorIds` or use `[]` if you skipped Step B).

4. **ADMIN delete:** Authorize as `admin` / `admin123`, `DELETE /api/movies/1` → `204`.
5. **USER denied delete:** Authorize as `user`, `DELETE /api/movies/1` → `403` (or `404` if the movie was already deleted).
6. **ADMIN dashboard:** `GET /api/admin/summary` as admin → `200`; as user → `403`.

