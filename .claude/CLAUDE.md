# ecommerce-api

## Stack
- Java 21
- Spring Boot 4.1.0
- Maven
- MySQL

## Package Structure
Base package: `com.marom.ecommerce`

Sub-packages:
- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`
- `config`
- `security` — JWT auth support (`JwtService`, `AppUserDetails(Service)`, `CurrentUserService`, `JwtRolesConverter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`)

## Architecture Rules
- Always use DTOs in controllers — never expose entities directly.
- All business logic lives in the service layer — keep controllers thin.
- Centralised exception handling via `GlobalExceptionHandler`.
- All endpoints prefixed with `/api/v1/`.

## Security
- Stateless JWT bearer auth: `spring-boot-starter-security` + `-oauth2-resource-server`,
  HS256 shared secret (`app.security.jwt.*`; override `JWT_SECRET` in real envs).
- `POST /api/v1/auth/login` and `/register` issue a token; `GET /api/v1/auth/me` returns
  the current user. Config in `config/SecurityConfig` (authorization matrix) — public
  catalog reads, `ROLE_ADMIN` for all management, `ROLE_CUSTOMER` for placing orders /
  posting reviews.
- `users` table (1:0..1 to `customers` via nullable `customer_id`); `Role` enum name is
  the authority (`ROLE_ADMIN` / `ROLE_CUSTOMER`).
- The customer for an order/review is taken from the authenticated principal via
  `CurrentUserService`, **not** a `customerId` in the request body.
- Product pictures live under `/api/v1/products/{productId}/pictures` (like reviews): reads
  are public (incl. `GET .../{id}/content`), and upload/update/delete are `ROLE_ADMIN` —
  already covered by the broad `POST/PUT/DELETE /api/v1/products/**` rules. Bytes are stored
  in the `product_pictures` table (`LONGBLOB`); metadata reads use projection queries so the
  BLOB never loads during catalog reads. Uploads are `multipart/form-data` (`files` parts);
  oversize → 413, bad content type / empty / picture-limit → 422.
- 401/403 from the filter chain are rendered as `ErrorResponse` by
  `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`; `@PreAuthorize` denials and
  Spring Security auth exceptions are mapped in `GlobalExceptionHandler`.

## Lombok
- `@RequiredArgsConstructor` for injection everywhere (with `final` fields). No `@Autowired`.
- Entities: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`.
- DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.

## Exceptions
All handled in `GlobalExceptionHandler`, returning `ErrorResponse` JSON:

| Exception                    | HTTP Status |
|------------------------------|-------------|
| `ResourceNotFoundException`  | 404         |
| `BusinessRuleException`      | 422         |
| `DuplicateResourceException` | 409         |
| `AccessDeniedException`      | 403         |

## Database
- MySQL, database `ecommerce_db`.
- `ddl-auto=validate` — tables come from `schema.sql`, not Hibernate. Any new
  table/column (e.g. `product_pictures`) must be hand-added to `db/schema.sql` or startup
  validation fails.

## Commands
- Run: `./mvnw spring-boot:run`
- Test (unit + slice): `./mvnw test`
- Full verify (adds `SecurityEndToEndIT`, needs a seeded MySQL): `./mvnw verify`
- Compile check: `./mvnw clean compile`
