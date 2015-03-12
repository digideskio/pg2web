#!/bin/bash
echo $PGCONN

sed -i.bak -e "s/%PGCONN%/$PGCONN/g" /nginx/nginx/conf/nginx.conf
cat /nginx/nginx/conf/nginx.conf | grep dbname

/nginx/nginx/sbin/nginx
touch /nginx/nginx/logs/error.log
tailf  /nginx/nginx/logs/error.log
