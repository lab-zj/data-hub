#!/bin/bash

set -x
set -e

MINIO_ENDPOINT=${MINIO_ENDPOINT:-'http://host.docker.internal:9000'}
MINIO_ACCESSKEY=${MINIO_ACCESSKEY:-minioadmin}
MINIO_ACCESSSECRET=${MINIO_ACCESSSECRET:-minioadmin}
ALGORITHM_BUCKET=${ALGORITHM_BUCKET:-algorithms}
ALGORITHM_OBJECT_KEY=${ALGORITHM_OBJECT_KEY:-algorithm.tar.gz}

mc config host add minio ${MINIO_ENDPOINT} ${MINIO_ACCESSKEY} ${MINIO_ACCESSSECRET} \
    && mc cp minio/${ALGORITHM_BUCKET}/${ALGORITHM_OBJECT_KEY} /tmp/algorithm.tar.gz \
    && mkdir -p /app/code && tar -xf /tmp/algorithm.tar.gz -C /app/code \
    && echo 'algorithm-build-init success'
