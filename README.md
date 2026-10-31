# webapp
VoteEasy - web application

## Fast development loop (Maven + Docker database)

For everyday development, run Java/Maven on the host and only PostgreSQL in
Docker. This avoids rebuilding and packaging an image on every code change:

```bash
./sh/startLocal.sh
```

The script stops a running containerised webapp, starts only PostgreSQL,
waits for the development database to become ready, then runs Maven directly.
It works from any working directory and supports both `docker compose` and
legacy `docker-compose`. Existing database data is preserved. Ctrl+C stops
the application; PostgreSQL remains running for the next start.

Additional Maven arguments are forwarded, for example to use a different port:

```bash
./sh/startLocal.sh -Dspring-boot.run.arguments=--server.port=18080
```

Java 17 or newer is required; Maven is provided by the wrapper. The script runs
`./mvnw -Pdevelopment spring-boot:run` without `clean` or image packaging.
`sh/app.sh` remains available when the database is already running.
The development profile includes
Spring Boot DevTools, and templates/static assets are loaded from the source
tree. Java changes need to be compiled (for example by the IDE) for DevTools
to restart the application.

Do not use the `cleanUpDBAndStartDocker*.sh` scripts just to restart the app:
they remove the PostgreSQL container and attempt to delete its data volume.
Use the explicit reset script below when a fresh database is intended.

### Destructive reset and fast restart

**Warning: `sh/resetLocal.sh` permanently deletes all database data in this
Compose project, including both the `postgres` and `voteeasy` databases.**
It stops/removes the project's containers and volumes with
`docker compose down --volumes`, then invokes `sh/startLocal.sh` to create a
fresh PostgreSQL instance and start the application directly via Maven.
Liquibase and application bootstrap recreate the schema and initial data.
Unrelated Docker projects, images and build caches are not pruned.

Stop any locally running VoteEasy application with Ctrl+C first. Back up any
data you want to retain, then run:

```bash
./sh/resetLocal.sh
```

The script requires typing `DELETE` before making destructive changes.
Cancelling leaves containers and data unchanged. For automated preparation
of a **dedicated disposable E2E test instance**, skip the prompt explicitly:

```bash
./sh/resetLocal.sh --yes
```

A non-interactive invocation without `--yes` is refused. Additional Maven
arguments are forwarded, just as with the non-destructive script:

```bash
./sh/resetLocal.sh --yes -Dspring-boot.run.arguments=--server.port=18080
```

Both scripts use the Compose project selected by the repository's Compose
file and environment (including `COMPOSE_PROJECT_NAME`, if set). Use the
same project configuration as when the database was originally started.
Changing the HTTP port does **not** isolate the database or make a reset safe.
The reset script starts the application in the foreground; run the E2E tests
in a second terminal after startup has completed.

## Running locally with Docker Compose

Prerequisite: Docker with Docker Compose and BuildKit (enabled by default in
current Docker versions). Java and Maven do not need to be
installed on the host to build the container image.

```bash
docker compose up --build -d
```

This builds the web application from the current source code and starts it
once the database health check passes. The application is available at
<http://localhost:8080>.

If port 8080 is already used (for example by a local Maven run), choose a
free host port:

```bash
WEBAPP_PORT=18080 docker compose up --build -d
```

The application is then available at <http://localhost:18080>.

HTML templates and static assets (CSS, JavaScript and images) are included
in the application JAR. Compose configures Spring to load them from the
classpath; no source-directory mounts are required.
The application defaults to German on both the host and in Docker, including
login/logout redirects without a language parameter. Use `?lang=en` to select
English explicitly.

```bash
docker compose logs -f webapp
docker compose down
```

PostgreSQL data is preserved in the existing named volume.
The containerised web application uses the `voteeasy` database; the existing
local Maven setup uses the `postgres` database, as configured in
`application.properties`. An existing volume is neither deleted nor migrated.
The application runs the configured Liquibase changesets on startup.

The Docker build keeps Maven dependencies and the wrapper distribution in a
persistent BuildKit cache. The first build downloads them; subsequent builds
reuse them even when the source code changes. Compilation and JAR packaging
still happen in Docker, so the direct Maven development loop above is faster.
Pruning BuildKit caches makes the next build download dependencies again.

After changing the source code, run `docker compose up --build -d` again.
Plain `docker compose up` (or `docker-compose up`) reuses an existing image;
it does not rebuild it after source changes.

If login works but the welcome page returns HTTP 500 with
`Error resolving template [/app/index.html]`, rebuild the image to include
the corrected relative template names. The templates are present in the JAR;
view names must not start with `/` when loading them from the classpath.
No database-volume deletion is necessary.

The image build skips tests because the integration tests require Docker.
Run the tests separately:

```bash
./mvnw -Pdevelopment test
```

To run the web application via Maven, start only the database. If the
containerised web application is already running, stop it first with
`docker compose stop webapp`:

```bash
docker compose up -d postgres
./mvnw -Pdevelopment spring-boot:run
```

The Compose configuration uses fixed database credentials and is intended
for local development, not production use.
