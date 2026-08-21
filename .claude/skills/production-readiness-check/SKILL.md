---
name: production-readiness-check
description: Run a full production-readiness audit of this Spring Boot ecommerce API before shipping, deploying, or opening a release PR. Checks N+1 query risk, missing @Transactional on DB-writing service methods, entity leakage through controllers, exception-handling coverage against GlobalExceptionHandler, data-integrity and concurrency safety (race conditions in check-then-act code, unhandled constraint violations, destructive schema scripts), config/security hygiene (hardcoded secrets, ddl-auto setting, missing @Valid/bean-validation), and test coverage gaps — then produces one consolidated readiness report. Use this whenever the user asks things like "is this ready to ship", "production readiness check", "pre-launch check", "can we deploy this", "readiness review", "audit before release", or wants a general health check of the codebase before a milestone — even if they don't say "production readiness" explicitly.
---

# Production Readiness Check

This audits the whole codebase against the project's own conventions
(`.claude/CLAUDE.md`, `.claude/rules/*.md`) rather than generic best
practices — the point is to catch the specific ways *this* codebase tends to
drift from its own rules before that drift ships. It folds together the
project's three existing single-purpose checks with five additional
categories, into one report.

Work through the seven categories below, then assemble the report using the
template at the end. Skip a category's detail section only if it has nothing
to report — still mention it in the summary table as clean.

## 1. N+1 query risk

Invoke the `find-n-plus-one` skill and carry its findings into the report
under "N+1 Query Risk". It already knows how to scan `@Service` and
`@Repository` classes for lazy relationships fetched in `toResponse()`
mapping, collections accessed in loops, and missing `JOIN FETCH`/
`@EntityGraph` — no need to redo that work here.

One thing worth double-checking yourself even after invoking the sub-skill:
if a repository has more than one finder method (`findAll()`, `findById()`,
a custom `findByX()`), confirm the fix was applied to *all* of them, not
just the one that happened to be under test when the N+1 was last found. A
fix applied only to `findAll()` while `findById()` stays unannotated is an
easy regression to miss.

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

## 5. Data integrity & concurrency safety

Application-level checks (an `existsBy...`/`findBy...` lookup before a
`save()`) only prevent a conflict when requests run one at a time. Under
real concurrent traffic, two requests can both pass the check before either
commits, and the database's own unique constraint becomes the actual
enforcement mechanism — which matters because that failure mode is easy to
build correctly at the database level and then let leak through as a raw,
unhandled error at the application level. Two specific things to check:

- Find every check-then-act pattern in the service layer — a
  `findBy.../existsBy...` uniqueness check followed by a separate `save()`
  call (duplicate-prevention logic is the most common place this shows up),
  and any read-check-write update of a numeric field like a stock or
  inventory count with no `@Version` field on the entity and no pessimistic
  lock. Note where these exist; they're a race condition under concurrent
  requests, not a bug today, so treat this as a lower-severity finding than
  the next one.
- Check whether `GlobalExceptionHandler` has a handler for
  `DataIntegrityViolationException` (Spring's exception for a failed DB
  constraint). Without one, a losing request in a race like the above falls
  through to the generic 500 handler instead of the project's documented
  409 for duplicates — the status-code contract silently breaks exactly
  when the check-then-act logic above is the least effective. This is worth
  flagging even independently of whether you found a specific race, since
  it's a cheap, general safety net.

Also skim `db/schema.sql` (and any migration files) for destructive
statements — `DROP DATABASE`, `DROP TABLE` without a narrow scope — sitting
in a script that could plausibly be run against a real environment rather
than only a local dev reset. Flag it if there's nothing in the repo (a
separate prod migration path, a guard, a comment) distinguishing "safe to
run anywhere" from "local reset only."

## 6. Config & security hygiene

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

## 7. Test coverage gaps

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
`ddl-auto` not `validate`, a destructive script reachable from a real
deploy path).

```markdown
# Production Readiness Report — <project name>

## Summary

| Category                          | Status | Findings |
|-------------------------------------|--------|----------|
| N+1 Query Risk                       | ✅/⚠️/❌ | n |
| Transactional Coverage               | ✅/⚠️/❌ | n |
| DTO Coverage                          | ✅/⚠️/❌ | n |
| Exception Handling                    | ✅/⚠️/❌ | n |
| Data Integrity & Concurrency Safety   | ✅/⚠️/❌ | n |
| Config & Security Hygiene             | ✅/⚠️/❌ | n |
| Test Coverage                         | ✅/⚠️/❌ | n |

## N+1 Query Risk
<carried over from find-n-plus-one, or "No risks found.">

## Transactional Coverage
<carried over from check-transactions, or "All DB-writing methods are @Transactional.">

## DTO Coverage
<carried over from verify-dto-coverage, or "All controller methods return DTOs.">

## Exception Handling
<mismatches, unhandled types, missing fallback — or "All custom exceptions are handled with the correct status.">

## Data Integrity & Concurrency Safety
<check-then-act races, missing DataIntegrityViolationException handler, destructive schema scripts — or "No issues found.">

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
