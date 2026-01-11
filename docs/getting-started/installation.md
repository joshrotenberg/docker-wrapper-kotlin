# Installation

## Requirements

- **JDK 17+** (or JDK 21 for virtual threads support)
- **Docker CLI** in your PATH (or Podman, Colima, etc.)
- **Kotlin 2.0+** (for Kotlin projects)

## Gradle

=== "Kotlin DSL"

    ```kotlin
    dependencies {
        // Core module (required)
        implementation("io.github.joshrotenberg:docker-kotlin-core:0.1.0")
        
        // Optional modules
        implementation("io.github.joshrotenberg:docker-kotlin-compose:0.1.0")
        implementation("io.github.joshrotenberg:docker-kotlin-templates:0.1.0")
        implementation("io.github.joshrotenberg:docker-kotlin-redis:0.1.0")
    }
    ```

=== "Groovy DSL"

    ```groovy
    dependencies {
        // Core module (required)
        implementation 'io.github.joshrotenberg:docker-kotlin-core:0.1.0'
        
        // Optional modules
        implementation 'io.github.joshrotenberg:docker-kotlin-compose:0.1.0'
        implementation 'io.github.joshrotenberg:docker-kotlin-templates:0.1.0'
        implementation 'io.github.joshrotenberg:docker-kotlin-redis:0.1.0'
    }
    ```

## Maven

```xml
<dependencies>
    <!-- Core module (required) -->
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>docker-kotlin-core</artifactId>
        <version>0.1.0</version>
    </dependency>
    
    <!-- Optional modules -->
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>docker-kotlin-compose</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>docker-kotlin-templates</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>docker-kotlin-redis</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## Module Overview

### docker-kotlin-core

The core module provides:

- Command execution engine
- All Docker CLI commands (run, stop, rm, build, etc.)
- Platform detection (Docker, Podman, Colima, OrbStack)
- Error handling and retry support

This is the only required module.

### docker-kotlin-compose

Docker Compose support:

- `docker compose up`, `down`, `ps`, etc.
- Project and file configuration
- Profile support

### docker-kotlin-templates

Base abstractions for container templates:

- `Template` interface with lifecycle management
- Wait strategies for container readiness
- Auto-cleanup via `AutoCloseable`

### docker-kotlin-redis

Pre-configured Redis containers:

- `RedisTemplate` - Single Redis instance
- `RedisClusterTemplate` - Redis Cluster (planned)
- `RedisSentinelTemplate` - Redis Sentinel (planned)

## Verifying Installation

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.core.Docker

    suspend fun main() {
        val docker = Docker()
        val version = docker.version()
        println("Docker client: ${version.clientVersion}")
        println("Docker server: ${version.serverVersion}")
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.core.Docker;
    import io.github.joshrotenberg.dockerkotlin.core.command.VersionInfo;

    public class Main {
        public static void main(String[] args) {
            Docker docker = Docker.create();
            VersionInfo version = docker.versionBlocking();
            System.out.println("Docker client: " + version.getClientVersion());
            System.out.println("Docker server: " + version.getServerVersion());
        }
    }
    ```
