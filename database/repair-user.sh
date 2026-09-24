#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."
: "${MYSQL_ROOT_PASSWORD:?Set MYSQL_ROOT_PASSWORD before repairing the existing volume}"
MYSQL_USER="${MYSQL_USER:-hrms}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?Set MYSQL_PASSWORD before repairing the existing volume}"
MYSQL_DATABASE="${MYSQL_DATABASE:-hrms_db}"

docker compose exec -T mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -e "
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
GRANT ALL PRIVILEGES ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;"
