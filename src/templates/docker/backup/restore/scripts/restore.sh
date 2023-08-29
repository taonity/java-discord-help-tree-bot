#!/bin/bash

docker compose -f %{docker-compose.full-path} stop app
docker compose -f %{docker-compose.full-path} exec restore-backup runner "$1"
docker compose -f %{docker-compose.full-path} up -d --force-recreate
