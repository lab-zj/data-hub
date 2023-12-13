plugins {
    id("java")
    id("com.github.node-gradle.node") version ("3.0.1")
}

node {
    download.set(true)
    version.set("16.20.0")
    distBaseUrl.set("https://npmmirror.com/mirrors/node/")
}

group = "net.zjvis.flint.data.hub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register("buildBinary") {
    doLast {
        val resultMessagePathPropertyName = "RESULT_MESSAGE_PATH"
        if (!project.hasProperty(resultMessagePathPropertyName)) {
            throw RuntimeException("$resultMessagePathPropertyName not found in project property")
        }
        val resultMessageFile = project.file(project.property(resultMessagePathPropertyName) as String)
        resultMessageFile.writeText(project.buildDir.absolutePath)
    }
    dependsOn("npm_run_build")
}