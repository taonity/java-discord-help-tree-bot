#!/bin/bash

if [ -z $1 ];
then
  echo "You must provide backup file!"
  exit 0
fi

tar -xf "/archive/$1" -C /tmp

cp -r /tmp/backup/java-discord-help-bot-volume-db/current /backup/java-discord-help-bot-volume-db/archived
cp -r /tmp/backup/java-discord-help-bot-volume-gitea/current /backup/java-discord-help-bot-volume-gitea/archived