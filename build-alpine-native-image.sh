#!/bin/sh

mvn clean

docker run -it --rm --workdir /project --user "$(id -u):$(id -g)" --volume ${PWD}:/project graalvm-alpine sh --login -c "mvn -Pnative package"

mv target/geoip-server-linux-x86_64 target/geoip-server-alpine-x86_64
