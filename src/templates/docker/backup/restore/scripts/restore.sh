#!/bin/bash

docker compose -f docker/docker-compose.yml stop app
docker compose -f docker/docker-compose.yml exec restore-backup runner "$1"
docker compose -f docker/docker-compose.yml up -d --force-recreate
