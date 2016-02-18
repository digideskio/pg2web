FROM ubuntu:15.10
MAINTAINER Nikolay Ryzhikov <niquola@gmail.com>, \
           Pavel Bubentsov <bazzy.bazzy@gmail.com>, \
           Marat <muradbei@gmail.com>, \
           Danil Kutkevich <danil@kutkevich.org>

RUN apt-get update && apt-get install -yqq libreadline-dev libncurses5-dev libpcre3-dev libssl-dev perl make curl wget build-essential libpq-dev  unzip

ENV VERSION ngx_openresty-1.7.10.1
ENV URL http://openresty.org/download/$VERSION.tar.gz
ENV SOURCE_DIR /openresty
ENV BUILD_DIR /nginx

RUN mkdir $BUILD_DIR && mkdir $SOURCE_DIR && cd $SOURCE_DIR && curl -o openresty.tar.gz $URL && tar xzvf openresty.tar.gz && mv $VERSION openresty && rm -rf openresty.tar.gz

RUN apt-get install -yqq git
# replace with fixed ngx_postgres
RUN cd $SOURCE_DIR \
  && git clone https://github.com/niquola/ngx_postgres \
  && ls -lah $SOURCE_DIR/openresty/bundle \
  && rm -rf openresty/bundle/ngx_postgres-1.0rc5 \
  && ln -s $SOURCE_DIR/ngx_postgres openresty/bundle/ngx_postgres-1.0rc \
  && ls -lah openresty/bundle

RUN cd $SOURCE_DIR/openresty && ./configure --prefix=$BUILD_DIR --with-http_postgres_module -j8
RUN cd $SOURCE_DIR/openresty && make && make install

COPY nginx.conf $BUILD_DIR/nginx/conf/
COPY run.sh /
RUN chmod u+x /run.sh
ENV PGCONN 'postgres_server  127.0.0.1:5432 dbname=test user=test password=test'


############################### FHIRBASE ##########################
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN echo "LANG=en_US.UTF-8\nLANGUAGE=en_US.UTF-8\nLC_ALL=en_US.UTF-8\nLC_CTYPE=en_US.UTF-8" > /etc/default/locale
RUN apt-get -y -q install postgresql-9.4 postgresql-client-9.4 postgresql-contrib-9.4 postgresql-9.4-plv8

ENV DATABASE_URL postgres://root:root@localhost:5432/fhirbase

RUN echo 'until psql postgres -c "select 1"; do tail /var/log/postgresql/*.log; sleep 5; done;' > waitpg

RUN echo "plv8.start_proc='plv8_init'" >> /etc/postgresql/9.4/main/postgresql.conf
COPY seed.sql /

RUN /etc/init.d/postgresql start \
    && su postgres -c "psql --command \"CREATE USER root WITH SUPERUSER PASSWORD 'root';\"" \
    && bash waitpg \
    && createdb fhirbase \
    && wget https://github.com/fhirbase/fhirbase-plv8/releases/download/v0.0.1-beta.18/fhirbase-0.0.1-beta.18.sql.zip -O fhirbase.sql.zip \
    && unzip fhirbase.sql.zip \
    && cat fhirbase-0.0.1-beta.7.sql | psql fhirbase \
    && cat /seed.sql | psql fhirbase \
    && pg_ctlcluster -m smart 9.4 main stop

CMD /run.sh
