#!/bin/bash
set -e # Instantly aborts the script and fails the Jenkins stage if any command below fails

# Jenkins passes the TARGET_PORT (8081 or 8082) as the first argument ($1)
TARGET_PORT=$1

echo "Configuring Nginx to route traffic to port $TARGET_PORT..."

# Use sed to dynamically update the proxy_pass port in your Nginx config
sudo sed -i -E "s/proxy_pass http:\/\/localhost:[0-9]+;/proxy_pass http:\/\/localhost:${TARGET_PORT};/g" /etc/nginx/sites-available/default

# Test the config and reload Nginx without dropping traffic
sudo nginx -t && sudo systemctl reload nginx

echo "Traffic successfully switched to port $TARGET_PORT!"
