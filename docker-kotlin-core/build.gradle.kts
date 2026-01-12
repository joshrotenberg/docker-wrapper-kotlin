plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-library`
}

description = "Core Docker CLI wrapper for Kotlin/JVM"

dependencies {
    api(libs.coroutines.core)
    api(libs.serialization.json)
    api(libs.slf4j.api)
}
