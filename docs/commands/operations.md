# Container Operations

Commands for interacting with running containers.

## ExecCommand

Execute a command inside a running container.

=== "Kotlin"

    ```kotlin
    // Simple command
    val output = ExecCommand("my-container", "ls", "-la")
        .execute()
    println(output.stdout)
    
    // Interactive shell
    ExecCommand("my-container", "/bin/bash")
        .interactive()
        .tty()
        .execute()
    
    // With options
    ExecCommand("my-container", "python", "script.py")
        .user("www-data")           // run as user
        .workdir("/app")            // working directory
        .env("DEBUG", "true")       // environment variable
        .privileged()               // privileged mode
        .execute()
    ```

=== "Java"

    ```java
    ExecOutput output = ExecCommand.builder("my-container", "ls", "-la")
        .user("www-data")
        .workdir("/app")
        .executeBlocking();
    
    System.out.println(output.getStdout());
    ```

## LogsCommand

View container logs.

=== "Kotlin"

    ```kotlin
    // Get all logs
    val logs = LogsCommand("my-container")
        .execute()
    
    // Tail last 100 lines
    LogsCommand("my-container")
        .tail(100)
        .execute()
    
    // Follow logs (streaming)
    LogsCommand("my-container")
        .follow()
        .timestamps()
        .since("2024-01-01T00:00:00")
        .until("2024-01-02T00:00:00")
        .execute()
    ```

=== "Java"

    ```java
    LogsOutput logs = LogsCommand.builder("my-container")
        .tail(100)
        .timestamps()
        .executeBlocking();
    ```

## CpCommand

Copy files between host and container.

=== "Kotlin"

    ```kotlin
    // Copy from host to container
    CpCommand("/local/file.txt", "my-container:/app/file.txt")
        .execute()
    
    // Copy from container to host
    CpCommand("my-container:/app/data", "/local/data")
        .execute()
    
    // With options
    CpCommand("/local/dir", "my-container:/app/")
        .archive()      // preserve attributes
        .followLink()   // follow symlinks
        .execute()
    ```

=== "Java"

    ```java
    CpCommand.builder("/local/file.txt", "my-container:/app/file.txt")
        .archive()
        .executeBlocking();
    ```

## InspectCommand

Get detailed container information as JSON.

```kotlin
val info = InspectCommand("my-container")
    .execute()

println(info.id)
println(info.name)
println(info.state)
println(info.config)
println(info.networkSettings)
```

## PsCommand

List containers.

=== "Kotlin"

    ```kotlin
    // List running containers
    val containers = PsCommand()
        .execute()
    
    containers.forEach { container ->
        println("${container.id}: ${container.image} - ${container.status}")
    }
    
    // List all containers (including stopped)
    PsCommand()
        .all()
        .execute()
    
    // Filter by name, status, label
    PsCommand()
        .filter("name=web")
        .filter("status=running")
        .filter("label=app=nginx")
        .execute()
    
    // With size information
    PsCommand()
        .size()
        .execute()
    ```

=== "Java"

    ```java
    List<ContainerInfo> containers = PsCommand.builder()
        .all()
        .filter("status=running")
        .executeBlocking();
    ```

## TopCommand

Show processes running in a container.

```kotlin
val processes = TopCommand("my-container")
    .execute()

processes.forEach { process ->
    println("${process.pid}: ${process.command}")
}
```

## StatsCommand

Get live resource usage statistics.

```kotlin
val stats = StatsCommand("my-container")
    .noStream()  // single snapshot instead of streaming
    .execute()

println("CPU: ${stats.cpuPercent}%")
println("Memory: ${stats.memoryUsage} / ${stats.memoryLimit}")
println("Network I/O: ${stats.networkIn} / ${stats.networkOut}")
println("Block I/O: ${stats.blockRead} / ${stats.blockWrite}")
```

## PortCommand

Show port mappings for a container.

```kotlin
val ports = PortCommand("my-container")
    .execute()

ports.forEach { mapping ->
    println("${mapping.containerPort} -> ${mapping.hostIp}:${mapping.hostPort}")
}

// Get specific port
val port80 = PortCommand("my-container", 80)
    .execute()
```

## DiffCommand

Show filesystem changes in a container.

```kotlin
val changes = DiffCommand("my-container")
    .execute()

changes.forEach { change ->
    val type = when (change.type) {
        ChangeType.ADDED -> "A"
        ChangeType.MODIFIED -> "C"
        ChangeType.DELETED -> "D"
    }
    println("$type ${change.path}")
}
```

## CommitCommand

Create a new image from a container's changes.

```kotlin
val imageId = CommitCommand("my-container")
    .repository("my-repo")
    .tag("v1.0")
    .author("John Doe <john@example.com>")
    .message("Added configuration files")
    .change("ENV DEBUG=false")
    .change("EXPOSE 8080")
    .pause(false)  // don't pause during commit
    .execute()
```

## ExportCommand

Export container filesystem as a tar archive.

```kotlin
ExportCommand("my-container")
    .output("/path/to/archive.tar")
    .execute()
```

## AttachCommand

Attach to a running container's I/O streams.

```kotlin
AttachCommand("my-container")
    .stdin()   // attach STDIN
    .stdout()  // attach STDOUT
    .stderr()  // attach STDERR
    .noStdin() // don't attach STDIN
    .execute()
```

## Common Patterns

### Execute and Capture Output

```kotlin
val result = ExecCommand("my-container", "cat", "/etc/os-release")
    .execute()

if (result.exitCode == 0) {
    println(result.stdout)
} else {
    println("Error: ${result.stderr}")
}
```

### Tail Logs with Timestamps

```kotlin
LogsCommand("my-container")
    .tail(50)
    .timestamps()
    .execute()
```

### Backup Container Data

```kotlin
// Copy data out of container
CpCommand("db-container:/var/lib/postgresql/data", "/backup/pg-data")
    .archive()
    .execute()
```

### Create Image from Running Container

```kotlin
// Make changes in container
ExecCommand("my-container", "apt-get", "update").execute()
ExecCommand("my-container", "apt-get", "install", "-y", "vim").execute()

// Commit changes to new image
CommitCommand("my-container")
    .repository("my-image")
    .tag("with-vim")
    .message("Added vim editor")
    .execute()
```
