plugins {
    id("java")

    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
}

group = "net.zjvis.flint.data.hub"
version = "0.1-SNAPSHOT"

val lombokDependency = "org.projectlombok:lombok:1.18.22"
val openAPIVersion = "1.6.11"
val calciteVersion = "1.32.0"
val fabric8Version = "6.2.0"
val jenaVersion = "4.5.0"
val jGraphVersion = "1.5.1"
val jacksonVersion = "2.13.4"
val testContainerVersion = "1.17.5"
repositories {
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    maven { setUrl("https://maven.aliyun.com/repository/spring") }
    maven { setUrl("https://maven.aliyun.com/repository/mapr-public") }
    maven { setUrl("https://maven.aliyun.com/repository/spring-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
    mavenCentral()
}

dependencies {
    annotationProcessor(lombokDependency)
    compileOnly(lombokDependency)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.aliyun:dingtalk:1.3.53")
    implementation("org.apache.httpcomponents:httpclient:4.5.9")
    implementation("io.minio:minio:8.4.3")
    implementation("org.springdoc:springdoc-openapi-ui:${openAPIVersion}")
    implementation("tech.tablesaw:tablesaw-core:0.43.1")
    implementation("commons-io:commons-io:2.12.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.slf4j:slf4j-api:1.7.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("net.lingala.zip4j:zip4j:2.11.3")
    implementation("com.amihaiemil.web:eo-yaml:6.1.0")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("mysql:mysql-connector-java:8.0.30")
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.jooq:jooq:3.16.12")
    implementation("org.jgrapht:jgrapht-core:${jGraphVersion}")
    implementation("org.jgrapht:jgrapht-io:${jGraphVersion}")
    implementation("org.apache.calcite:calcite-csv:${calciteVersion}")
    implementation("org.apache.calcite:calcite-core:${calciteVersion}")
    implementation("org.apache.calcite:calcite-file:${calciteVersion}")
    implementation("org.apache.calcite:calcite-cassandra:${calciteVersion}")
    implementation("io.fabric8:kubernetes-client:${fabric8Version}")
    implementation("com.github.jsonld-java:jsonld-java:0.13.4")
    runtimeOnly("com.h2database:h2")

    testImplementation(lombokDependency)
    testCompileOnly(lombokDependency)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers:${testContainerVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testContainerVersion}")
}

tasks.test {
    useJUnitPlatform()
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    // comment this environment if you are using docker instead of podman
    environment(
        "DOCKER_HOST",
        "unix://${System.getProperties().get("user.home")}/.local/share/containers/podman/machine/qemu/podman.sock")
}

tasks.register("buildBinary") {
    doLast {
        val resultMessagePathPropertyName = "RESULT_MESSAGE_PATH"
        if (!project.hasProperty(resultMessagePathPropertyName)) {
            throw RuntimeException("$resultMessagePathPropertyName not found in project property")
        }
        val resultMessageFile = project.file(project.property(resultMessagePathPropertyName) as String)
        resultMessageFile.writeText(tasks.bootJar.get().archiveFile.get().asFile.absolutePath)
    }
    dependsOn(tasks.bootJar)
}