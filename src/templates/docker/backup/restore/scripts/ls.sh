#!/bin/bash

docker compose -f %{docker-compose.full-path} exec restore-backup bash -c 'ls -1 /archive'