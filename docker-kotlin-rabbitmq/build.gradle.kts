plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "RabbitMQ container templates for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-templates"))
}
