#!/bin/bash

# TODO: is it safe to dump online?

mkdir -p /tmp/current

cd /tmp/current

# TODO for some reason output logs go to error stream
gitea dump -c /data/gitea/conf/app.ini -f dump.zip