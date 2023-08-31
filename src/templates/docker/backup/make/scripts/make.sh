#!/bin/bash

POINT_VERSION="%{project.version}"
KEBAB_VERSION=$(echo "$POINT_VERSION" | tr . - )

docker compose -f %{docker-compose.full-path} exec make-backup /bin/sh -c 'export VERSION="'$KEBAB_VERSION'" && backup'
