#!/bin/sh

docker run -it --rm \
  --workdir /project \
  -e USER="${USER}" \
  -e UID="$(id -u)" \
  -e GID="$(id -g)" \
  --volume "${PWD}:/project" \
  graalvm-alpine \
  "mvn -Pnative clean package"

if [ -f "target/geoip-server-linux-aarch_64" ]; then
  mv -f target/geoip-server-linux-aarch_64 target/geoip-server-alpine-aarch64
fi

if [ -f "target/geoip-server-linux-x86_64" ]; then
  mv -f target/geoip-server-linux-x86_64 target/geoip-server-alpine-x86_64
fi