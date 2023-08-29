#!/bin/bash

# TODO: is it safe to dump online?

mkdir -p /tmp/current

cd /tmp/current

gitea dump -c /data/gitea/conf/app.ini -f dump.zip