#!/bin/sh -ux

while true; do
  redis-cli --cluster create 127.0.0.1:11001 127.0.0.1:11002 127.0.0.1:11003 127.0.0.1:11004 127.0.0.1:11005 127.0.0.1:11006 --cluster-yes --cluster-replicas 1
  if [ $? -eq 0 ]; then
    break
  fi
  sleep 1
done
