#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'

git pull

CONTAINER_NAME=stratego2evaluation
CONTAINER_VERSION=$(git rev-parse --short HEAD)
CONTAINER=${CONTAINER_NAME}:${CONTAINER_VERSION}

docker build -t ${CONTAINER}
docker run --rm -d -v ~/stratego2evaluation:/app/data ${CONTAINER}
