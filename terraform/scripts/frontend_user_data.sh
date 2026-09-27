#!/bin/bash
set -euxo pipefail

dnf install -y nginx
mkdir -p /var/www/frontend/releases/initial
cat > /var/www/frontend/releases/initial/index.html <<'HTML'
<!doctype html>
<html><head><title>Frontend is deploying</title></head><body>Frontend deployment is pending.</body></html>
HTML
ln -sfn /var/www/frontend/releases/initial /var/www/frontend/current
chmod 755 /var/www /var/www/frontend /var/www/frontend/releases /var/www/frontend/releases/initial
chmod 644 /var/www/frontend/releases/initial/index.html

cat > /etc/nginx/conf.d/frontend.conf <<'NGINX'
server {
    listen 80 default_server;
    server_name _;
    root /var/www/frontend/current;
    index index.html;

    location = /health {
        access_log off;
        default_type text/plain;
        return 200 "ok\n";
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX

rm -f /etc/nginx/conf.d/default.conf
systemctl enable --now nginx