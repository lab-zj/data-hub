#!/bin/bash

set -x
set -e

function if_env_variable_exists() {
    if [ ! $1 ]; then
        echo 'ENVIRONMENT VARIABLES IS NOT EXISTS'
        return 1
    fi
}

function minio_add_host() {
    if_env_variable_exists ${MINIO_ENDPOINT} \
    && if_env_variable_exists ${MINIO_ACCESSKEY} \
    && if_env_variable_exists ${MINIO_ACCESSSECRET} \
    && mc config host add minio ${MINIO_ENDPOINT} ${MINIO_ACCESSKEY} ${MINIO_ACCESSSECRET}
}

function minio_file_move() {
    if_env_variable_exists ${ALGORITHM_VALUES_PATH} \
    && mc cp minio/${ALGORITHM_VALUES_PATH} /app/algorithm.values.yaml
}

echo 'Operation target file move' \
    && minio_add_host && minio_file_move

