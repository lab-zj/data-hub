# Data-hub


## Getting started

1. [Deploy with docker](deploy.with.docker.md)
2. [Basic testing with curl](basic.testing.with.curl/README.md)
3. Visit http://localhost:10080 to play with frontend

## Manual

### How to deploy with docker

1. [Deploy with docker](deploy.with.docker.md)
2. Backend swagger api
    * http://localhost:10081/api/swagger-ui/index.html (if frontend is deployed)
    * http://localhost:18080/swagger-ui/index.html (only backend is deployed)

### How to debug backend

1. Prepare minio service
    * For example: using podman
        + ```shell
          podman run --rm \
              --name data-hub-minio \
              -p 9000:9000 \
              -p 9001:9001 \
              -d minio/minio:RELEASE.2022-09-07T22-25-02Z server /data --console-address :9001
          ```
2. Start backend with your favorite IDE or just run ```./gradlew backend:bootRun```
    * Default address for backend swagger api: http://localhost:8080/swagger-ui/index.html
