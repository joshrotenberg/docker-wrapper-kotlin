plugins {
    kotlin("jvm")
    `java-library`
}

description = "Core Docker CLI wrapper for Kotlin/JVM"

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.slf4j:slf4j-api")
}
