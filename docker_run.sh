#!/bin/bash
sudo docker build -t pg2web_2 .
sudo docker run --rm -it --env PGCONN='127.0.0.1:5432 dbname=fhirbase user=root password=root' -p 8888:80 pg2web_2
