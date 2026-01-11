# Commands Overview

docker-kotlin provides type-safe wrappers for Docker CLI commands. Each command uses a builder pattern with both Kotlin DSL and Java-friendly APIs.

## Command Categories

### Container Lifecycle

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `RunCommand` | `docker run` | Create and run a container |
| `CreateCommand` | `docker create` | Create a container without starting |
| `StartCommand` | `docker start` | Start a stopped container |
| `StopCommand` | `docker stop` | Stop a running container |
| `RestartCommand` | `docker restart` | Restart a container |
| `KillCommand` | `docker kill` | Force stop a container |
| `RmCommand` | `docker rm` | Remove a container |
| `PauseCommand` | `docker pause` | Pause a container |
| `UnpauseCommand` | `docker unpause` | Unpause a container |
| `WaitCommand` | `docker wait` | Wait for container to stop |
| `RenameCommand` | `docker rename` | Rename a container |

### Container Inspection

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `PsCommand` | `docker ps` | List containers |
| `LogsCommand` | `docker logs` | View container logs |
| `InspectCommand` | `docker inspect` | Get container details |
| `TopCommand` | `docker top` | Show running processes |
| `StatsCommand` | `docker stats` | Live resource usage |
| `PortCommand` | `docker port` | Show port mappings |
| `DiffCommand` | `docker diff` | Filesystem changes |

### Container Operations

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `ExecCommand` | `docker exec` | Execute command in container |
| `AttachCommand` | `docker attach` | Attach to container I/O |
| `CpCommand` | `docker cp` | Copy files to/from container |
| `CommitCommand` | `docker commit` | Create image from container |
| `ExportCommand` | `docker export` | Export container filesystem |

### Images

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `ImagesCommand` | `docker images` | List images |
| `PullCommand` | `docker pull` | Pull image from registry |
| `PushCommand` | `docker push` | Push image to registry |
| `BuildCommand` | `docker build` | Build image from Dockerfile |
| `TagCommand` | `docker tag` | Tag an image |
| `RmiCommand` | `docker rmi` | Remove image |
| `SaveCommand` | `docker save` | Save images to tar |
| `LoadCommand` | `docker load` | Load images from tar |
| `HistoryCommand` | `docker history` | Show image history |
| `SearchCommand` | `docker search` | Search Docker registry |

### Networks

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `NetworkCreateCommand` | `docker network create` | Create a network |
| `NetworkLsCommand` | `docker network ls` | List networks |
| `NetworkInspectCommand` | `docker network inspect` | Inspect a network |
| `NetworkConnectCommand` | `docker network connect` | Connect container to network |
| `NetworkDisconnectCommand` | `docker network disconnect` | Disconnect from network |
| `NetworkRmCommand` | `docker network rm` | Remove a network |
| `NetworkPruneCommand` | `docker network prune` | Remove unused networks |

### Volumes

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `VolumeCreateCommand` | `docker volume create` | Create a volume |
| `VolumeLsCommand` | `docker volume ls` | List volumes |
| `VolumeInspectCommand` | `docker volume inspect` | Inspect a volume |
| `VolumeRmCommand` | `docker volume rm` | Remove a volume |
| `VolumePruneCommand` | `docker volume prune` | Remove unused volumes |

### System

| Command | Docker CLI | Description |
|---------|-----------|-------------|
| `VersionCommand` | `docker version` | Docker version info |
| `InfoCommand` | `docker info` | System information |
| `EventsCommand` | `docker events` | Stream Docker events |
| `SystemDfCommand` | `docker system df` | Disk usage |
| `SystemPruneCommand` | `docker system prune` | Clean up resources |
| `LoginCommand` | `docker login` | Registry authentication |
| `LogoutCommand` | `docker logout` | Registry logout |

## Command Pattern

All commands follow a consistent pattern:

```kotlin
// 1. Create command with required parameters
val cmd = RunCommand("nginx:alpine")

// 2. Configure with builder methods
    .name("web-server")
    .port(8080, 80)
    .detach()

// 3. Execute (async or blocking)
    .execute()        // suspend fun
    .executeBlocking() // blocking fun
```

## Using Commands Directly vs Docker Class

You can use commands directly or through the `Docker` class:

=== "Direct Command"

    ```kotlin
    val containerId = RunCommand("nginx:alpine")
        .name("web")
        .port(8080, 80)
        .detach()
        .execute()
    ```

=== "Docker Class"

    ```kotlin
    val docker = Docker()
    
    val containerId = docker.run("nginx:alpine") {
        name("web")
        port(8080, 80)
        detach()
    }
    ```

The `Docker` class provides:

- Shared configuration (timeouts, platform detection)
- Cleaner Kotlin DSL syntax
- Consistent executor reuse

## Common Builder Methods

All commands support these methods:

| Method | Description |
|--------|-------------|
| `arg(String)` | Add a raw CLI argument |
| `args(vararg String)` | Add multiple raw CLI arguments |
| `timeout(Duration)` | Set execution timeout |
| `preview()` | Get command preview without executing |
| `execute()` | Execute asynchronously (suspend) |
| `executeBlocking()` | Execute synchronously (blocking) |

## Escape Hatches

For unmapped CLI options:

```kotlin
RunCommand("nginx:alpine")
    .name("web")
    .arg("--cgroupns=host")           // New Docker flag
    .args("--ulimit", "nofile=1024")  // Multiple args
    .execute()
```

## Return Types

Commands return typed results:

| Command | Return Type |
|---------|-------------|
| `RunCommand` | `ContainerId` |
| `VersionCommand` | `VersionInfo` |
| `PsCommand` | `List<ContainerInfo>` |
| `InspectCommand` | `InspectOutput` |
| `StopCommand` | `Unit` |
| `RmCommand` | `Unit` |
