plugins {
    kotlin("jvm")
    `java-library`
}

description = "Docker Compose support for docker-kotlin"

dependencies {
    api(project(":docker-kotlin-core"))

    // YAML generation
    implementation("org.yaml:snakeyaml:2.3")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.test {
    useJUnitPlatform()
}
