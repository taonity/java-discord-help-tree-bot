#!/bin/bash

mkdir -p /tmp/current

cd /tmp/current

gitea dump -c /data/gitea/conf/app.ini -f dump.zip