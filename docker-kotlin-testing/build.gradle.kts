plugins {
    kotlin("jvm")
    `java-library`
}

description = "Testing utilities for docker-kotlin with JUnit 5 integration"

dependencies {
    api(project(":docker-kotlin-templates"))

    // JUnit 5 for extensions
    api("org.junit.jupiter:junit-jupiter-api:5.11.4")

    // Optional: JUnit 5 engine for running tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}
