#!/bin/sh

# we MUST use a java home that equals the graalvm home
JAVA_HOME=$GRAALVM_HOME

# leverage tests w/ graalvm so it can generate necessary reflection info
mvn -Pnative -Dagent=true clean test

# target/native/agent-output/test/session-3840993-20220621T192036Z
# locate latest session of native reflect-config
AGENT_TARGET_DIR="target/native/agent-output/test"
SESSION_DIR_NAME=$(ls "$AGENT_TARGET_DIR" | head -1)
SESSION_DIR="$AGENT_TARGET_DIR/$SESSION_DIR_NAME"

echo "Session directory: $SESSION_DIR"

rm -f src/main/native-image/reflect-config.json
cp -v "$SESSION_DIR/reflect-config.json" src/main/native-image/reflect-config.json

rm -f src/main/native-image/resource-config.json
cp -v "$SESSION_DIR/resource-config.json" src/main/native-image/resource-config.json