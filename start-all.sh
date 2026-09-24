#!/usr/bin/env sh
set -eu

BACKEND_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
FRONTEND_DIR=$(CDPATH= cd -- "$BACKEND_DIR/../ShrijaAI_Model_Frontend-main" && pwd)
export PATH="/usr/local/bin:/opt/docker/bin:${PATH}"

services="mysql mcp auth employee attendance payroll manager leave hr document notification policy expense asset performance recruitment audit compliance workflow backend"
for service in $services; do
  echo "Building $service..."
  if [ "$service" = "payroll" ]; then
    "$BACKEND_DIR/mvnw" -B -ntp -pl payroll-agent -am package -DskipTests
    continue
  fi
  docker compose -f "$BACKEND_DIR/docker-compose.yml" build "$service"
done
docker compose -f "$BACKEND_DIR/docker-compose.yml" up -d --remove-orphans
docker compose -f "$FRONTEND_DIR/docker-compose.yml" up -d --build
docker compose -f "$BACKEND_DIR/docker-compose.yml" ps
docker compose -f "$FRONTEND_DIR/docker-compose.yml" ps
