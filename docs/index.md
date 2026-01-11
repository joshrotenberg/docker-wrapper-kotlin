# docker-kotlin

A type-safe Docker CLI wrapper for Kotlin/JVM with clean Java builder APIs and an idiomatic Kotlin DSL.

## Why docker-kotlin?

Existing JVM Docker libraries all use the Docker HTTP API directly. **docker-kotlin wraps the Docker CLI instead**, offering distinct advantages:

| Feature | docker-kotlin | API-based libraries |
|---------|--------------|---------------------|
| Works with Podman/Colima/OrbStack | Yes | Requires configuration |
| Docker Compose support | Native | Limited or none |
| TLS/Socket configuration | Not needed | Often complex |
| Mental model | CLI commands | API calls |

### When to use docker-kotlin

- **Development tooling** - CLI tools, dev scripts, local automation
- **Testing** - Integration tests, test fixtures, CI/CD pipelines
- **Docker Compose workflows** - Multi-container orchestration
- **Docker alternatives** - Podman, Colima, OrbStack, Rancher Desktop

### When to use API-based libraries

- **High-throughput production** - Many Docker operations per second
- **Low-latency requirements** - API calls are faster than process spawning
- **No CLI dependency** - Embedded or minimal environments

## Features

- **Full Docker CLI coverage** - All major commands with type-safe builders
- **Dual API surface** - Kotlin DSL + Java builder pattern
- **Async & blocking** - Coroutines for Kotlin, blocking methods for Java
- **Platform detection** - Auto-detects Docker, Podman, Colima, OrbStack
- **Compose support** - Native `docker compose` integration
- **Container templates** - Pre-configured containers for common services
- **Extensible** - Escape hatches for unmapped CLI options

## Quick Example

=== "Kotlin"

    ```kotlin
    val docker = Docker()

    // Run a container
    val containerId = docker.run("nginx:alpine") {
        name("web-server")
        port(8080, 80)
        env("WORKER_PROCESSES", "4")
        detach()
    }

    // Stop and remove
    docker.stop(containerId.value)
    docker.rm(containerId.value)
    ```

=== "Java"

    ```java
    Docker docker = Docker.create();

    // Run a container
    ContainerId containerId = RunCommand.builder("nginx:alpine")
        .name("web-server")
        .port(8080, 80)
        .env("WORKER_PROCESSES", "4")
        .detach()
        .executeBlocking();

    // Stop and remove
    StopCommand.builder(containerId.getValue()).executeBlocking();
    RmCommand.builder(containerId.getValue()).executeBlocking();
    ```

## Modules

| Module | Description |
|--------|-------------|
| `docker-kotlin-core` | Core CLI wrapper, commands, execution |
| `docker-kotlin-compose` | Docker Compose support |
| `docker-kotlin-templates` | Base template abstractions |
| `docker-kotlin-redis` | Redis container templates |

## License

Apache 2.0
