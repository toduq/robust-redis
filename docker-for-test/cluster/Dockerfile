FROM redis:6.2.6-alpine

RUN apk add --no-cache supervisor

ADD . /app

ENTRYPOINT supervisord -c /app/supervisord.conf -n
