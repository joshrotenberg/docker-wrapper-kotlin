# docker-kotlin

A type-safe Docker CLI wrapper for Kotlin/JVM.

[![CI](https://github.com/joshrotenberg/docker-wrapper-jvm/actions/workflows/ci.yml/badge.svg)](https://github.com/joshrotenberg/docker-wrapper-jvm/actions/workflows/ci.yml)
[![Documentation](https://github.com/joshrotenberg/docker-wrapper-jvm/actions/workflows/docs.yml/badge.svg)](https://joshrotenberg.github.io/docker-wrapper-jvm/)

## Overview

docker-kotlin provides a type-safe, idiomatic Kotlin API for interacting with Docker via the CLI. It wraps the `docker` command-line tool rather than using the Docker HTTP API.

## Why CLI over HTTP API?

| Approach | Pros | Cons |
|----------|------|------|
| **CLI wrapper** (this library) | Uses existing Docker installation; no additional dependencies; works with Docker Desktop, Podman, Colima; supports all CLI features immediately | Requires Docker CLI installed; shell process overhead |
| **HTTP API** (docker-java, etc.) | Direct daemon communication; no CLI dependency | Complex socket/TLS configuration; Docker Desktop compatibility issues; feature lag behind CLI |

This library is ideal when:
- You already have Docker CLI installed (most development environments)
- You want to avoid socket/TLS configuration complexity
- You need features that work across Docker Desktop, Podman, or Colima
- You prefer simple, predictable behavior matching CLI usage

## Why not Testcontainers?

[Testcontainers](https://testcontainers.org/) is excellent for integration testing with its extensive module ecosystem. docker-kotlin serves different use cases:

| Use Case | Testcontainers | docker-kotlin |
|----------|---------------|---------------|
| Pre-built database/service modules | Best choice | Not the focus |
| Custom container orchestration | Limited | Full control |
| Docker Compose workflows | Basic support | First-class DSL |
| Non-test Docker automation | Not designed for this | Primary use case |
| Learning Docker commands | Abstracts away | Maps 1:1 to CLI |

Use Testcontainers when you need ready-made modules for common services. Use docker-kotlin when you need direct Docker control, custom workflows, or Compose DSL capabilities.

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.joshrotenberg:docker-kotlin-core:0.1.0")
    
    // Optional modules
    implementation("io.github.joshrotenberg:docker-kotlin-compose:0.1.0")
    implementation("io.github.joshrotenberg:docker-kotlin-testing:0.1.0")
    implementation("io.github.joshrotenberg:docker-kotlin-templates:0.1.0")
}
```

## Quick Start

### Running Containers

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

// Pull and run a container
PullCommand("nginx:alpine").executeBlocking()

val containerId = RunCommand("nginx:alpine")
    .name("my-nginx")
    .publish(8080, 80)
    .detach()
    .executeBlocking()

// Execute commands in the container
ExecCommand(containerId, "nginx", "-v").executeBlocking()

// View logs
val logs = LogsCommand(containerId).tail(100).executeBlocking()

// Stop and remove
StopCommand(containerId).executeBlocking()
RmCommand(containerId).executeBlocking()
```

### Compose DSL

Define Docker Compose configurations in Kotlin:

```kotlin
import io.github.joshrotenberg.dockerkotlin.compose.dsl.*

val stack = dockerCompose {
    service("postgres") {
        image = "postgres:16-alpine"
        environment {
            "POSTGRES_USER" to "app"
            "POSTGRES_PASSWORD" to "secret"
            "POSTGRES_DB" to "mydb"
        }
        ports("5432:5432")
        healthcheck {
            testCmd("pg_isready", "-U", "app")
            interval = "10s"
            retries = 5
        }
    }
    
    service("redis") {
        image = "redis:7-alpine"
        ports("6379:6379")
    }
    
    volume("pgdata")
}

// Generate YAML
stack.writeTo("docker-compose.yml")

// Or run directly with automatic cleanup
stack.use("my-project") {
    // Stack is running, do your work
    // Automatically cleaned up when block exits
}
```

### Testing Support

JUnit 5 extension for container lifecycle management:

```kotlin
import io.github.joshrotenberg.dockerkotlin.testing.*

@ExtendWith(DockerExtension::class)
class MyIntegrationTest {

    @Container
    val redis = ContainerGuard(
        RunCommand("redis:7-alpine")
            .publish(6379, 6379)
    )

    @Test
    fun `test with redis`() {
        // Container is started before test, stopped after
        val client = Redis.connect("localhost", 6379)
        // ...
    }
}
```

## Modules

| Module | Description |
|--------|-------------|
| `docker-kotlin-core` | Core Docker commands (run, exec, build, etc.) |
| `docker-kotlin-compose` | Docker Compose commands and Kotlin DSL |
| `docker-kotlin-testing` | JUnit 5 extensions and test utilities |
| `docker-kotlin-templates` | Pre-configured container templates |
| `docker-kotlin-redis` | Redis-specific template and utilities |

## Supported Commands

### Container
`run`, `start`, `stop`, `restart`, `kill`, `pause`, `unpause`, `wait`, `rm`, `exec`, `attach`, `logs`, `top`, `stats`, `port`, `diff`, `cp`, `commit`, `export`, `create`, `update`, `rename`, `inspect`, `ps`

### Image
`pull`, `push`, `build`, `images`, `rmi`, `tag`, `save`, `load`, `import`, `search`, `history`, `prune`

### Network
`create`, `ls`, `rm`, `inspect`, `connect`, `disconnect`, `prune`

### Volume
`create`, `ls`, `rm`, `inspect`, `prune`

### System
`info`, `version`, `events`, `df`, `prune`

### Compose
`up`, `down`, `ps` (plus Kotlin DSL for configuration)

### Auth
`login`, `logout`

## Java Interoperability

All commands support blocking execution and builder patterns for Java:

```java
import io.github.joshrotenberg.dockerkotlin.core.command.*;

// Builder pattern
String containerId = RunCommand.builder("nginx:alpine")
    .name("my-nginx")
    .publish(8080, 80)
    .detach()
    .executeBlocking();

// Compose DSL via builders
ComposeSpec compose = ComposeBuilder.create()
    .service("web", web -> web
        .image("nginx:alpine")
        .ports("8080:80"))
    .build();
```

## Requirements

- JDK 17+
- Docker CLI installed and available in PATH
- Docker daemon running (Docker Desktop, Colima, Podman, etc.)

## Documentation

Full documentation: [https://joshrotenberg.github.io/docker-wrapper-jvm/](https://joshrotenberg.github.io/docker-wrapper-jvm/)

## License

Apache 2.0
