#!/bin/sh

mvn clean

docker run -it --rm --workdir /project --user "$(id -u):$(id -g)" --volume ${PWD}:/project graalvm-ubuntu bash --login -c "mvn -Pnative package"
