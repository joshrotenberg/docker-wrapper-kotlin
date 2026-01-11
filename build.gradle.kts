plugins {
    kotlin("jvm") version "2.1.0" apply false
    `java-library`
    `maven-publish`
}

allprojects {
    group = "io.github.joshrotenberg"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        val coroutinesVersion = "1.9.0"
        val slf4jVersion = "2.0.16"
        val junitVersion = "5.11.4"

        "implementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        "implementation"("org.slf4j:slf4j-api:$slf4jVersion")

        "testImplementation"(kotlin("test"))
        "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test")
        "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "testRuntimeOnly"("ch.qos.logback:logback-classic:1.5.12")
    }
}
