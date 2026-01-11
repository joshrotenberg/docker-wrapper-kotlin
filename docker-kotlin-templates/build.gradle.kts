plugins {
    kotlin("jvm")
    `java-library`
}

description = "Container template abstractions for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-core"))
}
