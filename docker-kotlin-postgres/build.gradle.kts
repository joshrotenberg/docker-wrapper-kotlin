plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "PostgreSQL container templates for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-templates"))
}
