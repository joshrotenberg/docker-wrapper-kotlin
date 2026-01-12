plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "MySQL container templates for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-templates"))
}
