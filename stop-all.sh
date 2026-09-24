#!/usr/bin/env sh
set -eu

BACKEND_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
FRONTEND_DIR=$(CDPATH= cd -- "$BACKEND_DIR/../ShrijaAI_Model_Frontend-main" && pwd)

docker compose -f "$FRONTEND_DIR/docker-compose.yml" down --remove-orphans
docker compose -f "$BACKEND_DIR/docker-compose.yml" down --remove-orphans
