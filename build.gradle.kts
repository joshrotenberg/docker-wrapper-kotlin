plugins {
    alias(libs.plugins.kotlin.jvm) apply false
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
        "implementation"(platform(rootProject.libs.coroutines.bom))
        "implementation"(rootProject.libs.coroutines.core)
        "implementation"(rootProject.libs.slf4j.api)

        "testImplementation"(platform(rootProject.libs.junit.bom))
        "testImplementation"(rootProject.libs.bundles.testing)
        "testRuntimeOnly"(rootProject.libs.junit.platform.launcher)
        "testRuntimeOnly"(rootProject.libs.logback.classic)
    }
}
