rootProject.name = "docker-kotlin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
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
include("docker-kotlin-testing")
