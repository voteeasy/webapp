#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

confirmed=false
if [[ "${1:-}" == "--help" ]]; then
    echo "Usage: $0 [--yes] [Maven arguments ...]"
    echo "Delete this Compose project's database volume and start local development."
    echo "WARNING: All data in the postgres and voteeasy databases will be lost."
    echo "Stop any locally running application before using this script."
    echo "--yes skips the interactive confirmation (for dedicated test instances only)."
    exit 0
elif [[ "${1:-}" == "--yes" ]]; then
    confirmed=true
    shift
fi

# Check prerequisites before deleting anything.
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

echo "WARNING: This will stop/remove this Compose project's containers and volumes."
echo "ALL existing database data (postgres AND voteeasy) will be permanently deleted."
echo "Stop any locally running VoteEasy JVM before continuing."
echo "Compose file: $PROJECT_ROOT/docker-compose.yml"

if [[ "$confirmed" != true ]]; then
    if [[ ! -t 0 ]]; then
        echo "Refusing a non-interactive reset without --yes." >&2
        exit 1
    fi
    read -r -p "Type DELETE to erase the database and restart: " answer
    if [[ "$answer" != "DELETE" ]]; then
        echo "Cancelled. No containers or database data were changed."
        exit 0
    fi
fi

# Scope deletion to this Compose project; never prune unrelated Docker data.
# Fail rather than starting with old data if the reset cannot be completed.
"${COMPOSE[@]}" -f "$PROJECT_ROOT/docker-compose.yml" down --volumes

# Reuse readiness checks and the fast Maven start, forwarding additional options.
exec "$PROJECT_ROOT/sh/startLocal.sh" "$@"
