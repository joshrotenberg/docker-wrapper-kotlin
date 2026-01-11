plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

description = "Core Docker CLI wrapper for Kotlin/JVM"

dependencies {
    api(libs.coroutines.core)
    api(libs.slf4j.api)
}
