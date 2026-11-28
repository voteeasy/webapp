# Agent instructions

## Scope and project overview

These instructions apply to the entire repository. Follow more specific
`AGENTS.md` instructions in subdirectories if any are introduced.

VoteEasy is a Java web application built with Spring Boot and Maven. It uses
PostgreSQL, Liquibase migrations and server-rendered HTML templates.

- Java source compatibility: Java 17; GitHub Actions currently uses Java 21.
- Use the Maven wrapper (`./mvnw`), not a separately installed Maven.
- Read `README.md` for local development and database setup.
- Write new documentation and changelog entries in British English.

## Repository layout

- `src/main/java/de/voteeasy/`: application code, organised by feature.
- `src/main/resources/templates/`: server-rendered HTML templates.
- `src/main/resources/static/`: CSS, JavaScript and images.
- `src/main/resources/db/changelog/`: Liquibase database migrations.
- `src/main/resources/`: application configuration and translation resources.
- `src/test/java/de/voteeasy/`: unit and integration tests.
- `src/test/resources/`: test configuration.
- `sh/`: local development and database management scripts.
- `.github/workflows/main.yml`: CI build configuration.
- `CHANGELOG.md`: notable changes, following Keep a Changelog 1.0.0.
- `target/`: generated build output; do not edit or commit it.

## Build and test

Specify a Maven goal explicitly. The profiles have different default goals,
which may also perform dependency checks or coverage instrumentation.

**Important:** the default `deployment` profile sets `maven.test.skip=true`.
A successful plain `./mvnw test` does not prove that tests ran. Use the
`development` profile or explicitly set `-Dmaven.test.skip=false`, and check the
reported test count and failures.

Run targeted tests first, for example:

```bash
./mvnw -Dmaven.test.skip=false \
  -Dtest=ValueTypeParserTest,SettingDatatypeIsValidValidatorTest,ReadSettingServiceTest test
```

Run the full test suite with a working Docker daemon:

```bash
./mvnw -Pdevelopment test
```

Integration tests use Testcontainers and PostgreSQL; see `AbstractIT` and
`VoteEasyDatabaseContainer`. They should not require resetting the local
Compose database.

To reproduce the CI build, including Clover instrumentation and installation:

```bash
./mvnw -B -Pdevelopment clean clover:instrument install
```

When reporting validation, distinguish tests that passed from tests that were
skipped or could not run. Do not claim a full-suite pass after only running
selected tests. Run `git diff --check` before handing changes back.

## Local development and data safety

- Prefer `./sh/startLocal.sh` for the host-based development loop. It starts
  PostgreSQL and runs the application via Maven without deleting existing data.
- Use `docker compose up --build -d` when a containerised application is needed.
  Existing images are not rebuilt by plain `docker compose up`.
- Do not run `sh/resetLocal.sh`, `cleanUpDBAndStartDocker*.sh`,
  `docker compose down --volumes` or other data-deleting commands without
  explicit authorisation. A different HTTP port does not isolate the database.
- Treat the Compose credentials as local-development configuration, not as
  production configuration. Never add production credentials or secrets.

## Making changes

- Inspect the relevant implementation, tests and configuration before editing.
- Keep changes focused; preserve the existing feature-based structure and code
  style. Avoid unrelated formatting and dependency updates.
- Preserve user changes. Do not discard or overwrite unrelated work, and do not
  commit or push unless explicitly asked.
- Add or update tests for changed behaviour, including invalid input, nulls and
  boundary values where relevant.
- When updating a dependency, check behaviour changes at its call sites rather
  than merely changing tests to accept new library behaviour.
- Validation must express the application's rules. `NUMERIC` settings accept
  ASCII whole numbers with an optional `+` or `-` sign and leading zeroes, within
  the Java `int` range. Use the regular expression and `Integer.parseInt()` to
  enforce this; do not replace them with `NumberUtils.isParsable()`.
  `ALPHANUMERIC` values may contain digits. Preserve the separate type checks.
- Add new Liquibase changesets for schema changes rather than rewriting
  previously applied migrations.
- Preserve the German default language and existing translation conventions.

## Changelog

Document notable changes under `Unreleased` in `CHANGELOG.md`, using the
appropriate Keep a Changelog category: `Added`, `Changed`, `Deprecated`,
`Removed`, `Fixed` or `Security`. Do not add empty categories or invent release
versions or dates.

Write entries in British English and describe user-visible behaviour, including
compatibility changes. Link related entries to their pull request when its
number is known, using the existing reference-link style (for example
`([PR #19])`). Do not invent a PR number or URL.

# Code conventions

* Do not use star imports.
* Write test assertions with assertJ.
