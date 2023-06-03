#!/bin/bash

docker compose -f docker/docker-compose.yml exec restore-backup bash -c 'ls /archive'