#!/usr/bin/env bash
set -euo pipefail

# Resolve paths relative to this script, not the caller's working directory.
PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if ! command -v java >/dev/null 2>&1 || ! java -version >/dev/null 2>&1; then
    echo "Java is required. Install a JDK (17 or newer) and configure JAVA_HOME." >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is required. Install and start Docker first." >&2
    exit 1
fi

if docker compose version >/dev/null 2>&1; then
    COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE=(docker-compose)
else
    echo "Docker Compose is required." >&2
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Docker is not available. Start Docker and try again." >&2
    exit 1
fi

# Do not build the webapp image or remove containers/volumes.
# Stop the containerised app to free port 8080 for the local JVM.
echo "** Stopping the containerised webapp (if running) ..."
"${COMPOSE[@]}" -f "$PROJECT_ROOT/docker-compose.yml" stop webapp

echo "** Starting PostgreSQL, preserving existing data ..."
"${COMPOSE[@]}" -f "$PROJECT_ROOT/docker-compose.yml" up -d postgres

echo "** Waiting for the local development database ..."
# application.properties uses the postgres database, not the voteeasy database.
ready=false
for ((attempt = 1; attempt <= 60; attempt++)); do
    if "${COMPOSE[@]}" -f "$PROJECT_ROOT/docker-compose.yml" exec -T postgres \
        pg_isready -h localhost -U postgres -d postgres >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 2
done

if [[ "$ready" != true ]]; then
    echo "PostgreSQL did not become ready within approximately 120 seconds." >&2
    "${COMPOSE[@]}" -f "$PROJECT_ROOT/docker-compose.yml" logs --tail=50 postgres >&2 || true
    exit 1
fi

echo "** Starting VoteEasy locally via Maven (no image build, no clean) ..."
echo "** Ctrl+C stops the application; PostgreSQL keeps running."
exec ./mvnw -Pdevelopment spring-boot:run "$@"
