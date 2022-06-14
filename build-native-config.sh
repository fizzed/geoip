#!/bin/sh

JAVA_HOME=/usr/lib/jvm/graalvm-ce-java17-22.1.0

# build shaded jar version first
mvn clean package

# start server in background
$JAVA_HOME/bin/java -agentlib:native-image-agent=config-output-dir=target/native-image-config -jar target/geoip-0.0.1-SNAPSHOT.jar --try-all-editions=true --static-data-file data/GeoLite2-City.mmdb &
APP_PID=$!

# let it startup in a few seconds
sleep 2

# hit the server once (one w/ a postal code is key)
curl http://localhost:18080/api/v1/ips/24.192.251.98

# kill the app (mimic CTRL-C)
kill -TERM $APP_PID
sleep 2

rm -f src/main/native-image/reflect-config.json
mv target/native-image-config/reflect-config.json src/main/native-image/reflect-config.json

rm -f src/main/native-image/resource-config.json
mv target/native-image-config/resource-config.json src/main/native-image/resource-config.json