ARCH=$(uname -p)
GRAALVM_ARCH="amd64"

echo "Detected arch $ARCH"

if [[ $ARCH == "aarch64" ]]; then
  GRAALVM_ARCH="aarch64"
  echo "Updating arch to aarch64!"
fi

docker build --build-arg GRAALVM_ARCH=$GRAALVM_ARCH --file Dockerfile.ubuntu --tag graalvm-ubuntu .
