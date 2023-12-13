#!/bin/sh

set -x
set -e

function helm_installation() {
    helm $1 --namespace ${ALGORITHM_NAMESPACE} \
        ${ALGORITHM_NAME} \
        ${ALGORITHM_CHART_URL} \
        --values /app/algorithm.values.yaml \
        --timeout ${ALGORITHM_HELM_TIMEOUT}s \
        --atomic
}

function callback() {
    TIMESTAMP=$(date +%s | sed -e 's/^ *//g' -e 's/ *$//g' -e '/^ *$/d')
    wget --quiet ${CALLBACK_ADDRESS} \
        --header 'Content-Type: application/json' \
        --post-data "{\"token\": \"${CALLBACK_TOKEN}\", \"succeed\": $1, \"timestamp\": ${TIMESTAMP}}"
}

echo "${ALGORITHM_NAME} installation---" \
    && helm status ${ALGORITHM_NAME} \
        --namespace ${ALGORITHM_NAMESPACE} \
    && (helm_installation upgrade && callback true || callback false ) \
    || (helm_installation install && callback true || callback false )