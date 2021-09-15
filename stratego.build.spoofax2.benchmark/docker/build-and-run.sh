#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'

CONTAINER=stratego2evaluation

git pull
docker build -t ${CONTAINER} .
docker run --rm -d -v ~/stratego2evaluation:/app/data ${CONTAINER}
