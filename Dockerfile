FROM ubuntu:14.10
MAINTAINER Nikolay Ryzhikov <niquola@gmail.com>, Pavel Bubentsov <bazzy.bazzy@gmail.com>

RUN apt-get update && apt-get install -yqq libreadline-dev libncurses5-dev libpcre3-dev libssl-dev perl make curl build-essential libpq-dev

ENV VERSION ngx_openresty-1.7.10.1
ENV URL http://openresty.org/download/$VERSION.tar.gz
ENV SOURCE_DIR /openresty
ENV BUILD_DIR /nginx

RUN mkdir $BUILD_DIR && mkdir $SOURCE_DIR && cd $SOURCE_DIR && curl -o openresty.tar.gz $URL && tar xzvf openresty.tar.gz && mv $VERSION openresty && rm -rf openresty.tar.gz

RUN cd $SOURCE_DIR/openresty && ./configure --prefix=$BUILD_DIR \
  --with-http_postgres_module \
  -j4 \
  && make && make install

COPY nginx.conf $BUILD_DIR/nginx/conf/
COPY run.sh /
RUN chmod u+x /run.sh
ENV PGCONN 'postgres_server  127.0.0.1:5432 dbname=test user=test password=test'
CMD /run.sh
