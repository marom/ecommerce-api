---
name: production-readiness-check
description: Run a full production-readiness audit of this Spring Boot ecommerce API before shipping, deploying, or opening a release PR. Checks N+1 query risk, missing @Transactional on DB-writing service methods, entity leakage through controllers, exception-handling coverage against GlobalExceptionHandler, config/security hygiene (hardcoded secrets, ddl-auto setting, missing @Valid/bean-validation), and test coverage gaps — then produces one consolidated readiness report. Use this whenever the user asks things like "is this ready to ship", "production readiness check", "pre-launch check", "can we deploy this", "readiness review", "audit before release", or wants a general health check of the codebase before a milestone — even if they don't say "production readiness" explicitly.
---

# Production Readiness Check

This audits the whole codebase against the project's own conventions
(`.claude/CLAUDE.md`, `.claude/rules/*.md`) rather than generic best
practices — the point is to catch the specific ways *this* codebase tends to
drift from its own rules before that drift ships. It folds together the
project's three existing single-purpose checks with four additional
categories, into one report.

Work through the six categories below, then assemble the report using the
template at the end. Skip a category's detail section only if it has nothing
to report — still mention it in the summary table as clean.

## 1. N+1 query risk

Invoke the `find-n-plus-one` skill and carry its findings into the report
under "N+1 Query Risk". It already knows how to scan `@Service` and
`@Repository` classes for lazy relationships fetched in `toResponse()`
mapping, collections accessed in loops, and missing `JOIN FETCH`/
`@EntityGraph` — no need to redo that work here.

## 2. Transactional coverage

Invoke the `check-transactions` skill and carry its findings into the report
under "Transactional Coverage".

## 3. DTO coverage

Invoke the `verify-dto-coverage` skill and carry its findings into the
report under "DTO Coverage".

## 4. Exception handling coverage

The project's exception table (`.claude/CLAUDE.md`) promises a specific HTTP
status for each custom exception type. A mismatch here means a client gets
the wrong status code in production, and a completely unhandled exception
type means they get a raw 500 with no `ErrorResponse` body.

- Read `GlobalExceptionHandler` and list which exception types it handles
  and with what status.
- List every exception class under the `exception` package.
- Cross-check: does every custom exception class have a handler? Does each
  handler's status match the project's table (`ResourceNotFoundException`
  → 404, `BusinessRuleException` → 422, `DuplicateResourceException` → 409,
  `AccessDeniedException` → 403)? Is there a catch-all fallback for
  unexpected exceptions so nothing leaks an unstructured stack trace?
- Spot-check a few `throw new` sites in the service layer to confirm they're
  throwing one of these mapped types rather than a raw
  `RuntimeException`/`IllegalStateException` that would fall through to the
  generic handler unnecessarily.

## 5. Config & security hygiene

- Find the Spring config files (`application.properties`/`.yml` and
  profile variants). Confirm `ddl-auto` is `validate`, per the project's
  database rule — anything else risks Hibernate silently altering schema
  against a real database.
- Grep those same config files for hardcoded secrets: a literal value
  (not an env var placeholder like `${DB_PASSWORD}`) assigned to a
  password/secret/key/token-shaped property.
- For each `@RestController` method that takes a `@RequestBody`, confirm the
  parameter is annotated `@Valid` — without it, Bean Validation on the DTO
  never runs and invalid input reaches the service layer.
- For each request DTO (the ones passed as `@RequestBody`), confirm
  required fields carry a validation annotation (`@NotNull`, `@NotBlank`,
  `@Size`, `@Positive`, etc. as appropriate to the field). A field with no
  constraint at all is the actual gap to flag — don't nitpick which
  specific annotation was chosen.

## 6. Test coverage gaps

Per the project's testing rule, every class should have a matching
`[ClassName]Test.java`. For every class under `service/` and
`controller/`, check whether `src/test/java/.../[ClassName]Test.java`
exists. List any that don't. This is a coverage gap, not a defect — treat
it as a lower-severity finding than the others.

## Report template

Use this structure. Keep the summary table to one line per category; put
detail (file:line, specific findings) underneath. Use ✅ for clean, ⚠️ for
findings worth fixing before shipping, ❌ for anything that would cause
incorrect behavior in production (wrong HTTP status, real secret committed,
`ddl-auto` not `validate`).

```markdown
# Production Readiness Report — <project name>

## Summary

| Category               | Status | Findings |
|-------------------------|--------|----------|
| N+1 Query Risk           | ✅/⚠️/❌ | n |
| Transactional Coverage   | ✅/⚠️/❌ | n |
| DTO Coverage              | ✅/⚠️/❌ | n |
| Exception Handling        | ✅/⚠️/❌ | n |
| Config & Security Hygiene | ✅/⚠️/❌ | n |
| Test Coverage             | ✅/⚠️/❌ | n |

## N+1 Query Risk
<carried over from find-n-plus-one, or "No risks found.">

## Transactional Coverage
<carried over from check-transactions, or "All DB-writing methods are @Transactional.">

## DTO Coverage
<carried over from verify-dto-coverage, or "All controller methods return DTOs.">

## Exception Handling
<mismatches, unhandled types, missing fallback — or "All custom exceptions are handled with the correct status.">

## Config & Security Hygiene
<ddl-auto issues, hardcoded secrets, missing @Valid/validation — or "No issues found.">

## Test Coverage
<classes missing a test file — or "Every service and controller class has a matching test class.">
```

Finish with a one- or two-sentence verdict directly to the user (not just
the table) — e.g. "Two blockers before shipping: a hardcoded DB password in
`application.yml` and `AccessDeniedException` isn't wired into
`GlobalExceptionHandler`. Everything else looks clean." A busy reader should
be able to stop at your verdict and only dig into the report for detail.
