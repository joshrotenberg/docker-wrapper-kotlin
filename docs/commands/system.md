# System Commands

Commands for Docker system information and maintenance.

## VersionCommand

Get Docker version information.

=== "Kotlin"

    ```kotlin
    val version = docker.version()
    
    println("Client: ${version.clientVersion}")
    println("Server: ${version.serverVersion}")
    println("API: ${version.apiVersion}")
    println("Go: ${version.goVersion}")
    println("OS/Arch: ${version.os}/${version.arch}")
    ```

=== "Java"

    ```java
    VersionInfo version = docker.versionBlocking();
    
    System.out.println("Client: " + version.getClientVersion());
    System.out.println("Server: " + version.getServerVersion());
    ```

## InfoCommand

Get system-wide Docker information.

```kotlin
val info = InfoCommand()
    .execute()

println("Containers: ${info.containers}")
println("  Running: ${info.containersRunning}")
println("  Paused: ${info.containersPaused}")
println("  Stopped: ${info.containersStopped}")
println("Images: ${info.images}")
println("Server Version: ${info.serverVersion}")
println("Storage Driver: ${info.storageDriver}")
println("Docker Root Dir: ${info.dockerRootDir}")
println("CPUs: ${info.cpus}")
println("Memory: ${info.memoryTotal}")
```

## SystemDfCommand

Show Docker disk usage.

```kotlin
val usage = SystemDfCommand()
    .execute()

println("Images: ${usage.images.size}, ${usage.images.totalSize}")
println("Containers: ${usage.containers.size}, ${usage.containers.totalSize}")
println("Volumes: ${usage.volumes.size}, ${usage.volumes.totalSize}")
println("Build Cache: ${usage.buildCache.size}, ${usage.buildCache.totalSize}")

// Verbose output with details
SystemDfCommand()
    .verbose()
    .execute()
```

## SystemPruneCommand

Remove unused data (containers, images, networks, volumes).

=== "Kotlin"

    ```kotlin
    // Prune with defaults (stopped containers, dangling images, unused networks)
    val result = SystemPruneCommand()
        .force()  // don't prompt
        .execute()
    
    println("Space reclaimed: ${result.spaceReclaimed}")
    
    // Prune everything including volumes
    SystemPruneCommand()
        .all()       // remove all unused images, not just dangling
        .volumes()   // also remove unused volumes
        .force()
        .execute()
    
    // Prune with filter
    SystemPruneCommand()
        .filter("until=24h")  // only prune items older than 24 hours
        .force()
        .execute()
    ```

=== "Java"

    ```java
    PruneResult result = SystemPruneCommand.builder()
        .all()
        .volumes()
        .force()
        .executeBlocking();
    
    System.out.println("Space reclaimed: " + result.getSpaceReclaimed());
    ```

## EventsCommand

Stream Docker events in real-time.

```kotlin
// Stream all events
EventsCommand()
    .stream { event ->
        println("${event.time}: ${event.type} ${event.action} - ${event.actor}")
    }

// Filter events
EventsCommand()
    .filter("type=container")
    .filter("event=start")
    .filter("event=stop")
    .since("2024-01-01T00:00:00")
    .until("2024-01-02T00:00:00")
    .stream { event ->
        println(event)
    }
```

## LoginCommand

Authenticate with a Docker registry.

=== "Kotlin"

    ```kotlin
    // Login to Docker Hub
    LoginCommand()
        .username("myuser")
        .password("mypassword")
        .execute()
    
    // Login to private registry
    LoginCommand()
        .server("registry.example.com")
        .username("myuser")
        .password("mypassword")
        .execute()
    
    // Login with password from stdin (more secure)
    LoginCommand()
        .server("registry.example.com")
        .username("myuser")
        .passwordStdin()
        .execute()
    ```

=== "Java"

    ```java
    LoginCommand.builder()
        .server("registry.example.com")
        .username("myuser")
        .password("mypassword")
        .executeBlocking();
    ```

## LogoutCommand

Log out from a Docker registry.

```kotlin
// Logout from Docker Hub
LogoutCommand()
    .execute()

// Logout from specific registry
LogoutCommand()
    .server("registry.example.com")
    .execute()
```

## Common Patterns

### Check Docker Health

```kotlin
suspend fun checkDockerHealth(): Boolean {
    return try {
        val version = docker.version()
        println("Docker is running: ${version.serverVersion}")
        true
    } catch (e: DockerException.DaemonNotRunning) {
        println("Docker daemon is not running")
        false
    } catch (e: DockerException.DockerNotFound) {
        println("Docker is not installed")
        false
    }
}
```

### Disk Usage Report

```kotlin
val usage = SystemDfCommand()
    .verbose()
    .execute()

println("=== Docker Disk Usage ===")
println()

println("IMAGES:")
usage.images.forEach { image ->
    println("  ${image.repository}:${image.tag} - ${image.size}")
}
println("  Total: ${usage.images.totalSize}")
println()

println("CONTAINERS:")
usage.containers.forEach { container ->
    println("  ${container.name} - ${container.size}")
}
println("  Total: ${usage.containers.totalSize}")
println()

println("VOLUMES:")
usage.volumes.forEach { volume ->
    println("  ${volume.name} - ${volume.size}")
}
println("  Total: ${usage.volumes.totalSize}")
```

### Automated Cleanup

```kotlin
// Clean up resources older than 7 days
SystemPruneCommand()
    .filter("until=168h")  // 7 days in hours
    .all()
    .volumes()
    .force()
    .execute()
```

### Monitor Container Events

```kotlin
// Monitor container lifecycle events
EventsCommand()
    .filter("type=container")
    .stream { event ->
        when (event.action) {
            "start" -> println("Container started: ${event.actor.name}")
            "stop" -> println("Container stopped: ${event.actor.name}")
            "die" -> println("Container died: ${event.actor.name} (exit ${event.actor.exitCode})")
            "kill" -> println("Container killed: ${event.actor.name}")
        }
    }
```

### Registry Authentication Flow

```kotlin
// Login before push
LoginCommand()
    .server("ghcr.io")
    .username(System.getenv("GITHUB_USER"))
    .password(System.getenv("GITHUB_TOKEN"))
    .execute()

// Push image
PushCommand("ghcr.io/myuser/myapp:latest")
    .execute()

// Logout when done
LogoutCommand()
    .server("ghcr.io")
    .execute()
```
