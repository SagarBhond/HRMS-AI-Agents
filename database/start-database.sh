#!/usr/bin/env sh
set -eu

docker compose -f "$(dirname "$0")/../docker-compose.yml" up -d mysql
docker compose -f "$(dirname "$0")/../docker-compose.yml" ps mysql
