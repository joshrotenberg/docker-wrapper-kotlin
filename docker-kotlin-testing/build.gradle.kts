plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "Testing utilities for docker-kotlin with JUnit 5 integration"

dependencies {
    api(project(":docker-kotlin-templates"))
    api(libs.junit.jupiter.api)

    testRuntimeOnly(libs.junit.jupiter.engine)
}
