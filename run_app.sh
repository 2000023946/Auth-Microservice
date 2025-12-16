#!/bin/bash
# run-app.sh
# This script stops any running containers, removes volumes, and starts the app in test/dev mode.

# Fail fast if any command fails
set -e

# Select environment: default to test if not provided
ENV_FILE=".env.test"

# Stop and remove existing containers and volumes
echo "Stopping existing containers and removing volumes..."
docker-compose down -v

# Build and start the app
echo "Starting app in test/dev mode using $ENV_FILE..."
docker-compose --env-file $ENV_FILE up --build
