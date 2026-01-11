plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "Docker Compose support for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-core"))
    implementation(libs.snakeyaml)
}
