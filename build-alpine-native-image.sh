#!/bin/sh

docker run -it --rm \
  --workdir /project \
  -e USER="${USER}" \
  -e UID="$(id -u)" \
  -e GID="$(id -g)" \
  --volume "${PWD}:/project" \
  graalvm-alpine \
  "mvn -Pnative clean package"

mv target/geoip-server-linux-x86_64 target/geoip-server-alpine-x86_64
