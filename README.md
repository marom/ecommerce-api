# ecommerce-api

A small REST API for an e-commerce catalog and ordering flow — products,
categories, customers, orders, payments, and product reviews.

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Actuator) |
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
  config/       app configuration
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

| Method | Path | Description |
|---|---|---|
| `GET`    | `/api/v1/products` | List products |
| `POST`   | `/api/v1/products` | Create a product |
| `GET`    | `/api/v1/products/{id}` | Get a product |
| `PUT`    | `/api/v1/products/{id}` | Update a product |
| `DELETE` | `/api/v1/products/{id}` | Delete a product |
| `GET`    | `/api/v1/categories` | List categories |
| `POST`   | `/api/v1/categories` | Create a category |
| `GET`    | `/api/v1/categories/{id}` | Get a category |
| `PUT`    | `/api/v1/categories/{id}` | Update a category |
| `DELETE` | `/api/v1/categories/{id}` | Delete a category |
| `GET`    | `/api/v1/customers` | List customers |
| `POST`   | `/api/v1/customers` | Create a customer |
| `GET`    | `/api/v1/customers/{id}` | Get a customer |
| `PUT`    | `/api/v1/customers/{id}` | Update a customer |
| `DELETE` | `/api/v1/customers/{id}` | Delete a customer |
| `GET`    | `/api/v1/orders` | List orders |
| `POST`   | `/api/v1/orders` | Place an order |
| `GET`    | `/api/v1/orders/{id}` | Get an order |
| `PUT`    | `/api/v1/orders/{id}/status` | Change order status |
| `GET`    | `/api/v1/payments/{id}` | Get a payment |
| `PUT`    | `/api/v1/payments/{id}/complete` | Mark a payment complete |
| `GET`    | `/api/v1/products/{productId}/reviews` | List reviews for a product |
| `POST`   | `/api/v1/products/{productId}/reviews` | Post a review (one per customer per product) |

### Interactive docs

- Swagger UI: <http://localhost:8080/swagger-ui.html>
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
| `AccessDeniedException` | `403 Forbidden` |

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
