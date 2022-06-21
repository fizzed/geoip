ARCH=$(uname -p)
GRAALVM_ARCH="amd64"

if [ "$ARCH" -eq "aarch64" ]; then
  GRAALVM_ARCH="aarch64"
fi

docker build --build-arg GRAALVM_ARCH=$GRAALVM_ARCH --file Dockerfile.ubuntu --tag graalvm-ubuntu .
