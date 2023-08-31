#!/bin/bash

# unzip your backup file within the container
unzip -q -d /tmp/archived/dump /tmp/archived/dump.zip
cd /tmp/archived/dump
# restore the gitea data
cp -rf /tmp/archived/dump/data/* /data/gitea
# restore the repositories itself
cp -rf /tmp/archived/dump/repos/* /data/git/repositories/
# adjust file permissions
chown -R git:git /data
# Regenerate Git Hooks
su -c "/usr/local/bin/gitea -c '/data/gitea/conf/app.ini' admin regenerate hooks" git
rm -r /tmp/archived/dump
echo "Gitea successfully restored!"