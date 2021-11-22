#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'

IMAGE_NAME=stratego2evaluation
IMAGE_VERSION=$(git rev-parse --short HEAD)
IMAGE=${IMAGE_NAME}:${IMAGE_VERSION}

CONTAINER=${IMAGE_NAME}_${IMAGE_VERSION}_${RANDOM}

docker build -t ${IMAGE} .
docker run -d --name ${CONTAINER} -e TARGET=${TARGET:-} -e CONTAINER=${CONTAINER} --env-file .env ${IMAGE}
# docker container cp ${CONTAINER}:/home/myuser/data/benchmark_results/ ~/stratego2evaluation/.
# docker container rm ${CONTAINER}
