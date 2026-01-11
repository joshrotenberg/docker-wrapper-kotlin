# Docker Compose Overview

The `docker-kotlin-compose` module provides support for Docker Compose operations.

## Installation

=== "Kotlin DSL"

    ```kotlin
    dependencies {
        implementation("io.github.joshrotenberg:docker-kotlin-compose:0.1.0")
    }
    ```

=== "Groovy DSL"

    ```groovy
    dependencies {
        implementation 'io.github.joshrotenberg:docker-kotlin-compose:0.1.0'
    }
    ```

## Quick Start

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.compose.*

    // Start services
    ComposeUpCommand()
        .file("docker-compose.yml")
        .projectName("myapp")
        .detach()
        .execute()

    // Check status
    val services = ComposePsCommand()
        .file("docker-compose.yml")
        .execute()

    services.forEach { service ->
        println("${service.name}: ${service.status}")
    }

    // Stop and clean up
    ComposeDownCommand()
        .file("docker-compose.yml")
        .volumes()
        .execute()
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.compose.*;

    // Start services
    ComposeUpCommand.builder()
        .file("docker-compose.yml")
        .projectName("myapp")
        .detach()
        .executeBlocking();

    // Check status
    List<ComposeServiceInfo> services = ComposePsCommand.builder()
        .file("docker-compose.yml")
        .executeBlocking();

    // Stop and clean up
    ComposeDownCommand.builder()
        .file("docker-compose.yml")
        .volumes()
        .executeBlocking();
    ```

## Key Concepts

### Project Name

The project name is used as a prefix for container names. By default, it's derived from the directory name.

```kotlin
ComposeUpCommand()
    .file("docker-compose.yml")
    .projectName("myproject")  // containers named myproject-service-1
    .execute()
```

### Multiple Compose Files

Compose supports merging multiple files:

```kotlin
ComposeUpCommand()
    .file("docker-compose.yml")
    .file("docker-compose.override.yml")
    .file("docker-compose.prod.yml")
    .execute()
```

### Profiles

Use profiles to selectively enable services:

```yaml
# docker-compose.yml
services:
  web:
    image: nginx

  debug:
    image: busybox
    profiles:
      - debug
```

```kotlin
ComposeUpCommand()
    .file("docker-compose.yml")
    .profile("debug")  // includes debug service
    .execute()
```

### Environment Files

Load environment variables from files:

```kotlin
ComposeUpCommand()
    .file("docker-compose.yml")
    .envFile(".env")
    .envFile(".env.local")
    .execute()
```

## Available Commands

| Command | Description |
|---------|-------------|
| `ComposeUpCommand` | Create and start containers |
| `ComposeDownCommand` | Stop and remove containers, networks |
| `ComposePsCommand` | List containers |
| `ComposeLogsCommand` | View output from containers |
| `ComposeExecCommand` | Execute command in running container |
| `ComposeRunCommand` | Run one-off command |
| `ComposeBuildCommand` | Build or rebuild services |
| `ComposePullCommand` | Pull service images |
| `ComposePushCommand` | Push service images |
| `ComposeStartCommand` | Start services |
| `ComposeStopCommand` | Stop services |
| `ComposeRestartCommand` | Restart services |
| `ComposeKillCommand` | Kill containers |
| `ComposePauseCommand` | Pause services |
| `ComposeUnpauseCommand` | Unpause services |
| `ComposeConfigCommand` | Validate and view config |
| `ComposeTopCommand` | Display running processes |
| `ComposeEventsCommand` | Stream container events |
| `ComposePortCommand` | Print port bindings |
| `ComposeImagesCommand` | List images |
| `ComposeCpCommand` | Copy files |
| `ComposeScaleCommand` | Scale services |
| `ComposeWaitCommand` | Wait for services |

## Global Options

All compose commands support these options:

```kotlin
ComposeUpCommand()
    // File options
    .file("docker-compose.yml")
    .projectName("myapp")
    .projectDirectory("/path/to/project")
    
    // Environment
    .envFile(".env")
    .profile("production")
    
    // Output control
    .progress(ProgressType.PLAIN)
    
    // Modes
    .compatibility()  // run in compatibility mode
    .dryRun()         // show what would happen
    
    .execute()
```

## Next Steps

- [Commands Reference](commands.md) - Detailed command documentation
