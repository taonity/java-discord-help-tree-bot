#!/bin/bash

if [ ! -f /backup/archived/dump.sql ];
then
 echo "There is no dump file!"
 exit 0
fi

psql -U ${POSTGRES_USER} -f /shared/drop_db.sql -f /backup/archived/dump.sql postgres;