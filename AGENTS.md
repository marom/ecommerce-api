# AGENTS.md

Spring Boot 4.1.0 (Java 21, Maven, MySQL) REST API. Base package `com.marom.ecommerce`; layered `controller` → `service` → `repository` + `entity`, `dto`, `exception` (`GlobalExceptionHandler`), `config`, `security` (JWT).

## Commands
- `./mvnw test` — unit + `@WebMvcTest` slices. No DB required. **Does not boot JPA/Hibernate.**
- `./mvnw verify` — also runs `SecurityEndToEndIT` (full Spring context). **Requires a seeded MySQL on localhost:3306, root/empty password** (`mysql -u root < db/schema.sql` then `mysql -u root < db/example-data.sql`). CI runs exactly this with a MySQL service.
- `./mvnw clean compile` — compile-only check. No linter/formatter configured.

## Critical gotcha
`@WebMvcTest` slices skip the JPA context, so entity-mapping / Hibernate bootstrap errors (bad annotations, schema mismatches) pass `./mvnw test` and only fail under `./mvnw verify`. Always run `verify` before opening a PR.

## Database
- `ddl-auto=validate`: Hibernate does NOT create or alter tables. Schema is only `db/schema.sql` (DDL), demo rows in `db/example-data.sql`. Any new table/column must be hand-added to `schema.sql` or startup fails.
- Local MySQL is localhost:3306 root/empty. docker-compose maps MySQL to host port **3307** and auto-loads both SQL files on first start.
- Demo logins are seeded by `db/example-data.sql` (e.g. `admin@shop.example.com` / `admin123`).

## Conventions
- DI: `@RequiredArgsConstructor` + `final` fields; never `@Autowired`.
- Entities: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` (never `@Data`). DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.
- Money is always `BigDecimal`; never `double`/`float`.
- Controllers expose DTOs only (never entities), endpoints prefixed `/api/v1`.
- Errors: throw custom exceptions; `GlobalExceptionHandler` maps ResourceNotFound→404, BusinessRule→422, Duplicate→409, AccessDenied→403, validation→400, as `ErrorResponse` JSON.

## Security
- Stateless JWT (HS256, OAuth2 resource server). Secret from `app.security.jwt.secret` / `JWT_SECRET` env (≥32 chars in real envs).
- The customer for an order/review is the **authenticated caller** via `CurrentUserService` — never read a `customerId` from the request body.
- `ROLE_ADMIN` manages everything; `ROLE_CUSTOMER` places orders / posts reviews. Authorization matrix in `config/SecurityConfig.java`.
- Product picture bytes live in `product_pictures.data` (LONGBLOB). Catalog reads use projection queries (`ProductPictureView`) so the BLOB never loads; only `/content` serves bytes.

## Query discipline (N+1)
- List reads use `@EntityGraph`/`JOIN FETCH`, not default `findAll()`: `OrderRepository` fetches `{customer, payment}`; `ProductRepository.findAll` fetches `category`; `ReviewRepository` joins `customer`; `UserRepository` fetches `customer`.
- `Order.items` and the `Product` entity carry `@BatchSize(size = 100)`. **Hibernate 7 rejects `@BatchSize` on a to-one attribute** (`Property 'X' may not be annotated '@BatchSize'`) — to batch lazy to-one loads, annotate the target entity class instead of the field.
- `ProductService.getAllProducts()` batch-loads pictures via `findViewsByProductIdIn(ids)` — keep this pattern in new list endpoints.
- Keep `toResponse(...)` mapping inside the `@Transactional` method so lazy fetches stay in-session.

## Tests
- JUnit 5 + Mockito + AssertJ (`assertThat` only — never `assertEquals`), `MockMvc`.
- Controllers: `@WebMvcTest` + `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`) for each dependency.
- Naming: `[ClassName]Test.java`, methods `should_X_when_Y`, AAA sections with `// Arrange // Act // Assert` comments.

## Git workflow
- PR-based GitHub flow: work on short-lived branches, open a PR, merge via GitHub. Branch names use a type prefix: `perf/`, `docs/`, `fix/`, `chore/`, `feature/`.
- CI (GitHub Actions) runs `./mvnw verify` against a seeded MySQL service. Always run `verify` locally before pushing/opening a PR.

## Repo tooling (do not delete)
- `.claude/hooks/guardrail.sh` blocks any file edit containing `@Autowired` (enforces the DI convention at hook level).
- `.claude/agents/webmvc-test-writer.md` — subagent spec for writing `@WebMvcTest` controller tests.
- `.playwright-mcp/` — Playwright MCP config for storefront/browser-level testing.


## my preferences
- after finishing add line:
-- DONE --
