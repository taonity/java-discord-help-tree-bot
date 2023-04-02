#!/bin/bash

echo Hello

tar -xf "/archive/$1" -C /tmp

cp -r /tmp/backup/prodenv-mc-data/current /backup/prodenv-mc-data/archived