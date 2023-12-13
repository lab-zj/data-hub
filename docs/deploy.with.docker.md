# deploy with podman

1. backend
    * ```shell
      # @formatter:off
      # compile
      podman run --rm \
          --name data-hub-backend-compile \
          -v $(pwd):/app \
          -v $HOME/gradle-home:/root/.gradle \
          --workdir /app \
          -it gradle:8.2.1-jdk11-focal ./gradlew :backend:bootJar
      # prepare minio service
      podman run --rm \
          --name data-hub-minio \
          -p 9000:9000 \
          -p 9001:9001 \
          -d minio/minio:RELEASE.2022-09-07T22-25-02Z server /data --console-address :9001
      # run as service
      podman run --rm \
          --name data-hub-backend \
          -p 18080:8080 \
          -v $(pwd)/backend/build/libs/backend-0.1-SNAPSHOT.jar:/app/backend.jar:ro \
          -e MINIO_HOST=host.containers.internal \
          --workdir /app \
          -d gradle:8.2.1-jdk11-focal java -jar /app/backend.jar
      ```
2. visit http://localhost:18080/swagger-ui.html to check api docs
3. frontend
    * ```shell
      # compile, NOTE: this will create node-modules volume to hold frontend/node_modules
      podman run --rm \
          --name data-hub-frontend-compile \
          -v $(pwd):/app \
          -v $HOME/gradle-home:/root/.gradle \
          -v node-modules:/app/frontend/node_modules \
          --workdir /app \
          -it gradle:8.2.1-jdk11-focal ./gradlew :frontend:npm_run_build
      # run as service, NOTE: not choosing 10080 as port for chrome considers it as unsafe port
      podman run --rm \
          --name data-hub-frontend \
          -p 10081:80 \
          -v $(pwd)/frontend/build:/usr/share/nginx/html:ro \
          -v $(pwd)/frontend/local-nginx.conf:/etc/nginx/conf.d/default.conf:ro \
          -d nginx:1.19.9-alpine
      ```
4. visit http://localhost:10080 to check frontend
