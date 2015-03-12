# pg2web

docker run --rm --env PGCONN='172.17.0.12:5777 dbname=fhirbase user=fhirbase password=fhirbase' -p 8888:80 -t pgweb
