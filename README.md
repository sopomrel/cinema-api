# Cinema API — Spring Boot Midterm + Security Assignment

REST API for managing movies, actors, and directors, secured with Spring Security.

## Run the application

```bash
mvn spring-boot:run
```

- API root: `http://localhost:8080/` — welcome JSON with useful links
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cinemadb`, user: `sa`, no password)

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

> **Important:** A movie requires an existing **director** (`directorId` is mandatory). **Actors** are optional (`actorIds`). Create directors and actors **before** creating a movie.

### Setup (do this first)

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

