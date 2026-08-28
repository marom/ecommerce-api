# ecommerce-api

A small REST API for an e-commerce catalog and ordering flow — products,
categories, customers, orders, payments, product reviews, and product pictures.

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Actuator) |
| Security | Spring Security + OAuth2 Resource Server (stateless JWT, HS256) |
| Build | Maven (`./mvnw`) |
| Database | MySQL 8.4 |
| API docs | springdoc-openapi (Swagger UI) |

## Layout

```
src/main/java/com/marom/ecommerce/api/
  controller/   thin REST controllers, DTOs in/out only
  service/      business logic, @Transactional boundaries
  repository/   Spring Data JPA repositories
  entity/       JPA entities (tables come from db/schema.sql, not Hibernate)
  dto/          request/response payloads
  exception/    custom exceptions + GlobalExceptionHandler
  config/       app configuration (SecurityConfig, CorsConfig, OpenApiConfig)
  security/     JWT issue/validate, current-user resolution, 401/403 handlers
db/schema.sql   authoritative schema + seed data
```

## Configuration

`src/main/resources/application.properties` defaults to a local MySQL:

```
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=validate
```

`ddl-auto=validate` means the schema is **not** managed by Hibernate — load
`db/schema.sql` yourself (or use Docker Compose, which does it for you).
Any property can be overridden with an environment variable, e.g.
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

### JWT secret

HS256 signing uses `app.security.jwt.secret` (default in `application.properties` is a
throwaway dev value). **Set `JWT_SECRET` (≥ 32 chars) in every real environment.**
`app.security.jwt.expiration-seconds` (default 3600) and `app.security.jwt.issuer` are
also configurable.

## Running

### With Docker Compose (api + MySQL)

```bash
cp .env.example .env        # set MYSQL_ROOT_PASSWORD; keep MYSQL_DATABASE=ecommerce_db
docker compose up -d --build
```

- API on <http://localhost:8080>, MySQL on host port **3307** (to avoid clashing
  with a local 3306).
- MySQL loads `db/schema.sql` on first start; data persists in the `mysql-data` volume.
- The API container waits for MySQL to pass a health check before starting.
- Secrets live in `.env` (gitignored); the compose file holds only `${VAR}` references.

```bash
docker compose down       # stop
docker compose down -v    # stop and wipe the database volume
```

### Locally against your own MySQL

```bash
mysql -u root < db/schema.sql          # creates ecommerce_db + seed data
./mvnw spring-boot:run
```

## API

Base path: `/api/v1`. All endpoints consume/produce JSON.

### Authentication

Stateless JWT bearer tokens. `POST /api/v1/auth/login` returns
`{ "accessToken": "...", "tokenType": "Bearer", "expiresIn": 3600, "role": "...", "customerId": ... }`;
send it as `Authorization: Bearer <token>` on protected calls.

Seeded accounts (`db/schema.sql`, **demo passwords — change before any real use**):

| Email | Password | Role | Linked customer |
|---|---|---|---|
| `admin@shop.example.com` | `admin123` | `ROLE_ADMIN` | — |
| `john.doe@example.com` | `password123` | `ROLE_CUSTOMER` | John Doe (id 1) |
| `jane.smith@example.com` | `password123` | `ROLE_CUSTOMER` | Jane Smith (id 2) |
| `ravi.kumar@example.com` | `password123` | `ROLE_CUSTOMER` | Ravi Kumar (id 3) |

`POST /api/v1/auth/register` creates a new `ROLE_CUSTOMER` account (with a linked
customer record) and returns a token. In Swagger UI, click **Authorize** and paste the
token.

> **Contract change:** orders and reviews no longer take a `customerId` in the request
> body — the customer is the authenticated caller. A customer sees/manages only their own
> orders; admins see all. Admin accounts have no linked customer, so they cannot place
> orders or post reviews.

| Method | Path | Access | Description |
|---|---|---|---|
| `POST`   | `/api/v1/auth/login` | public | Obtain an access token |
| `POST`   | `/api/v1/auth/register` | public | Register a customer, get a token |
| `GET`    | `/api/v1/auth/me` | authenticated | Current user |
| `GET`    | `/api/v1/products`, `/api/v1/products/{id}` | public | List / get products |
| `POST` `PUT` `DELETE` | `/api/v1/products{/id}` | `ROLE_ADMIN` | Create / update / delete a product |
| `GET`    | `/api/v1/categories`, `/api/v1/categories/{id}` | public | List / get categories |
| `POST` `PUT` `DELETE` | `/api/v1/categories{/id}` | `ROLE_ADMIN` | Create / update / delete a category |
| `GET` `POST` `PUT` `DELETE` | `/api/v1/customers{/id}` | `ROLE_ADMIN` | Manage customers |
| `POST`   | `/api/v1/orders` | `ROLE_CUSTOMER` | Place an order (as the caller) |
| `GET`    | `/api/v1/orders` | authenticated | Own orders (customer) / all orders (admin) |
| `GET`    | `/api/v1/orders/{id}` | authenticated | Own order (customer) / any (admin) |
| `PUT`    | `/api/v1/orders/{id}/status` | `ROLE_ADMIN` | Change order status |
| `GET`    | `/api/v1/payments/{id}` | `ROLE_ADMIN` | Get a payment |
| `PUT`    | `/api/v1/payments/{id}/complete` | `ROLE_ADMIN` | Mark a payment complete |
| `GET`    | `/api/v1/products/{productId}/reviews` | public | List reviews for a product |
| `POST`   | `/api/v1/products/{productId}/reviews` | `ROLE_CUSTOMER` | Post a review (as the caller; one per product) |
| `GET`    | `/api/v1/products/{productId}/pictures` | public | List a product's pictures |
| `GET`    | `/api/v1/products/{productId}/pictures/{id}/content` | public | Download a picture's raw bytes |
| `POST`   | `/api/v1/products/{productId}/pictures` | `ROLE_ADMIN` | Upload one or more pictures (`multipart/form-data`, `files` parts) |
| `PUT`    | `/api/v1/products/{productId}/pictures/{id}` | `ROLE_ADMIN` | Update a picture's alt text / display order |
| `DELETE` | `/api/v1/products/{productId}/pictures/{id}` | `ROLE_ADMIN` | Delete a picture |
| `GET`    | `/api/v1/users` | `ROLE_ADMIN` | List user accounts |
| `PUT`    | `/api/v1/users/{id}/role` | `ROLE_ADMIN` | Change a user's role |

### Interactive docs

- Swagger UI: <http://localhost:8080/swagger-ui.html> (use **Authorize** with a bearer token)
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

### Health

- <http://localhost:8080/actuator/health>

### Error responses

All errors return an `ErrorResponse` JSON body via `GlobalExceptionHandler`:

| Condition | Status |
|---|---|
| Bean-validation failure | `400 Bad Request` (lists field errors) |
| `ResourceNotFoundException` | `404 Not Found` |
| `BusinessRuleException` | `422 Unprocessable Entity` |
| `DuplicateResourceException` | `409 Conflict` |
| `AccessDeniedException` (app or Spring Security) | `403 Forbidden` |
| Missing / invalid / expired token | `401 Unauthorized` |
| Bad credentials on login | `401 Unauthorized` |

## Tests

```bash
./mvnw test
```

Unit tests use JUnit 5 + Mockito + AssertJ; controllers are covered with
`@WebMvcTest` slices. JaCoCo coverage is written to
`target/site/jacoco/index.html`.

## Container image

A production `Dockerfile` is included: multi-stage (Temurin 21 JDK build →
Temurin 21 JRE runtime), layered jar, non-root user, and a container
health check against `/actuator/health`.

```bash
docker build -t ecommerce-api .
```
