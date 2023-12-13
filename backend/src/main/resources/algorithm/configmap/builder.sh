#!/bin/sh

set -x
set -e

function image_build() {
    DOCKER_REGISTRY=${DOCKER_REGISTRY:-'docker.io'}
    ALGORITHM_NAME=${ALGORITHM_NAME:-algorithm-demo}
    ALGORITHM_VERSION=${ALGORITHM_VERSION:-2022.11.18-r0}
    docker build \
        --build-arg BASE_PYTHON_REGISTRY=${BASE_PYTHON_REGISTRY} \
        --build-arg BASE_PYTHON_IMAGE=${BASE_PYTHON_IMAGE} \
        --build-arg BASE_PYTHON_TAG=${BASE_PYTHON_TAG} \
        --tag ${DOCKER_REGISTRY}/docker.io/${ALGORITHM_NAME}:${ALGORITHM_VERSION} \
        --file $1 $2 \
    && docker push ${DOCKER_REGISTRY}/docker.io/${ALGORITHM_NAME}:${ALGORITHM_VERSION}
}

function callback() {
    TIMESTAMP=$(date +%s | sed -e 's/^ *//g' -e 's/ *$//g' -e '/^ *$/d')
    CALLBACK_ADDRESS=${CALLBACK_ADDRESS:-'http://10.105.20.64:32086/server/algorithm/v1/callback'}
    CALLBACK_TOKEN=${CALLBACK_TOKEN:-'82b1143a0fd32db03749504176b1bf51'}
    wget --quiet ${CALLBACK_ADDRESS} \
       --header 'Content-Type: application/json' \
       --post-data "{\"token\": \"${CALLBACK_TOKEN}\", \"succeed\": $1, \"timestamp\": ${TIMESTAMP}}" \
    && killall docker-init
}

while true
do
    docker info && break || sleep 5
done

mkdir -p /app/builder \
    && mv /app/code/src /app/builder \
    && mv /app/code/requirements.txt /app/builder \
    && cp /app/bin/entry-point.sh /app/builder/entry-point.sh \
    && image_build /app/bin/Dockerfile /app/builder \
    && callback true \
    || callback false
