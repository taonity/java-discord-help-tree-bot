#!/bin/bash

echo Hello

tar -xf "/archive/$1" -C /tmp

cp -r /tmp/backup/java-discord-help-bot-volume-db/current /backup/java-discord-help-bot-volume-db/archived