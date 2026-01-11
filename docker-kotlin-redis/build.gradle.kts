plugins {
    kotlin("jvm")
    `java-library`
}

description = "Redis container templates for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-templates"))
}
