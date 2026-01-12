rootProject.name = "docker-kotlin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}

include("docker-kotlin-core")
include("docker-kotlin-compose")
include("docker-kotlin-templates")
include("docker-kotlin-redis")
include("docker-kotlin-postgres")
include("docker-kotlin-mysql")
include("docker-kotlin-mongodb")
include("docker-kotlin-rabbitmq")
include("docker-kotlin-nginx")
include("docker-kotlin-testing")
