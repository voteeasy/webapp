# webapp
VoteEasy - web application

## Running locally with Docker Compose

Prerequisite: Docker with Docker Compose. Java and Maven do not need to be
installed on the host to build the container image.

```bash
docker compose up --build -d
```

This builds the web application from the current source code and starts it
once the database health check passes. The application is available at
<http://localhost:8080>.

```bash
docker compose logs -f webapp
docker compose down
```

PostgreSQL data is preserved in the existing named volume.
The containerised web application uses the `voteeasy` database; the existing
local Maven setup uses the `postgres` database, as configured in
`application.properties`. An existing volume is neither deleted nor migrated.
The application runs the configured Liquibase changesets on startup.

After changing the source code, run `docker compose up --build -d` again.
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
