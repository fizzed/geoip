all: run

#run: alpine-native-run
run: alpine-native-released-run

#build: alpine-native-build
build: alpine-native-released-image

run_local: alpine-native-run-local

clean:
	docker-compose run --rm alpine_builder mvn clean
	docker-compose down


### Alpine builds ###
# overkill, but generally forces rebuilds (with cache)

alpine-native-builder: container/Dockerfile-alpine
	docker-compose build alpine_builder

alpine-native-image: container/Dockerfile-alpine
	docker-compose build alpine_native_image

# Runs the maven build process with results in target/
alpine-native-build: alpine-native-builder
	docker-compose run --rm alpine_native

# Starts the native exe container image
alpine-native-run: alpine-native-image
	#-docker-compose run --rm -p 18888:18888 alpine_native_image
	docker-compose up alpine_native_image

alpine-native-run-local: alpine-native-build
	docker-compose up alpine_native_run_local


## Simple images for released binaries

alpine-native-released-image:
	docker-compose build alpine_released

alpine-native-released-run:
	-docker-compose up alpine_released
