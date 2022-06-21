#!/bin/sh

mvn clean

docker run -it --rm --workdir /project --user "$(id -u):$(id -g)" --volume ${PWD}:/project graalvm-alpine sh --login -c "mvn -Pnative package"

if [ -f "target/geoip-server-linux-aarch_64" ]; then
  mv -f target/geoip-server-linux-aarch_64 target/geoip-server-alpine-aarch64
fi

if [ -f "target/geoip-server-linux-x86_64" ]; then
  mv -f target/geoip-server-linux-x86_64 target/geoip-server-alpine-x86_64
fi