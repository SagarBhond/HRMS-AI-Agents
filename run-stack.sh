#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"
test -f .env || { echo "Missing .env. Copy .env.example to .env and set local secrets."; exit 1; }

docker compose --env-file .env up -d mysql
docker compose --env-file .env up -d mcp auth
docker compose --env-file .env up -d employee leave document notification
docker compose --env-file .env up -d backend

frontend="$(cd .. && pwd)/ShrijaAI_Model_Frontend-main"
docker compose -f "$frontend/docker-compose.yml" up -d --build
docker compose ps
