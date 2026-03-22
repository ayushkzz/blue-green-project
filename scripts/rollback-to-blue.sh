#!/bin/bash
echo "Rolling back traffic to BLUE (port 8081)..."

sudo bash -c 'cat > /etc/nginx/sites-available/blue-green <<NGINX
upstream active {
    server localhost:8081;
}

server {
    listen 80;

    location / {
        proxy_pass http://active;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }

    location /health {
        proxy_pass http://active/health;
    }
}
NGINX'

sudo nginx -t && sudo systemctl reload nginx
echo "Rolled back to BLUE successfully!"
