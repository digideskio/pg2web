#!/bin/bash
echo $PGCONN

sed -i.bak -e "s/%PGCONN%/$PGCONN/g" /nginx/nginx/conf/nginx.conf
cat /nginx/nginx/conf/nginx.conf | grep dbname

/nginx/nginx/sbin/nginx
service postgresql start
psql fhirbase -c "SET plv8.start_proc = 'plv8_init';"
touch /nginx/nginx/logs/error.log
tailf  /nginx/nginx/logs/error.log
