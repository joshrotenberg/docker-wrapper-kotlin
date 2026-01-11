# Container Lifecycle Commands

Commands for managing container lifecycle: create, start, stop, remove.

## RunCommand

Create and start a container in one operation.

=== "Kotlin"

    ```kotlin
    val containerId = docker.run("nginx:alpine") {
        // Container identity
        name("web-server")
        hostname("web")
        
        // Port mappings
        port(8080, 80)                    // host:container
        port(8443, 443, Protocol.TCP)     // with protocol
        dynamicPort(3000)                 // auto-assign host port
        
        // Environment
        env("NODE_ENV", "production")
        env(mapOf("KEY1" to "val1", "KEY2" to "val2"))
        
        // Volumes
        volume("/host/path", "/container/path")
        volume("/config", "/etc/config", "ro")  // read-only
        namedVolume("data-volume", "/data")
        
        // Resource limits
        memory("512m")
        memorySwap("1g")
        cpus("1.5")
        cpuShares(512)
        
        // Networking
        network("my-network")
        networkAlias("web")
        
        // Restart policy
        restart(RestartPolicy.OnFailure(maxRetries = 3))
        
        // Other options
        detach()          // run in background
        rm()              // auto-remove on exit
        init()            // use init as PID 1
        privileged()      // privileged mode
        user("1000:1000") // run as user
        workdir("/app")   // working directory
        
        // Override entrypoint/command
        entrypoint("/bin/sh")
        command("-c", "echo hello")
        
        // Labels
        label("app", "web")
        labels(mapOf("env" to "prod", "team" to "platform"))
        
        // Platform
        platform("linux/amd64")
    }
    ```

=== "Java"

    ```java
    ContainerId containerId = RunCommand.builder("nginx:alpine")
        .name("web-server")
        .hostname("web")
        .port(8080, 80)
        .env("NODE_ENV", "production")
        .volume("/host/path", "/container/path")
        .memory("512m")
        .cpus("1.5")
        .network("my-network")
        .restart(RestartPolicy.OnFailure(3))
        .detach()
        .rm()
        .executeBlocking();
    ```

### Restart Policies

```kotlin
// Never restart (default)
restart(RestartPolicy.No)

// Always restart
restart(RestartPolicy.Always)

// Restart unless manually stopped
restart(RestartPolicy.UnlessStopped)

// Restart on failure with optional max retries
restart(RestartPolicy.OnFailure())
restart(RestartPolicy.OnFailure(maxRetries = 5))
```

### Port Protocols

```kotlin
port(8080, 80)                    // TCP (default)
port(8080, 80, Protocol.TCP)      // Explicit TCP
port(53, 53, Protocol.UDP)        // UDP
```

## StopCommand

Stop a running container.

=== "Kotlin"

    ```kotlin
    docker.stop("my-container")
    
    // With options
    docker.stop("my-container") {
        time(30)            // seconds to wait before killing
        signal("SIGTERM")   // signal to send
    }
    ```

=== "Java"

    ```java
    StopCommand.builder("my-container")
        .time(30)
        .signal("SIGTERM")
        .executeBlocking();
    ```

## RmCommand

Remove a container.

=== "Kotlin"

    ```kotlin
    docker.rm("my-container")
    
    // With options
    docker.rm("my-container") {
        force()    // force removal of running container
        volumes()  // remove associated anonymous volumes
    }
    ```

=== "Java"

    ```java
    RmCommand.builder("my-container")
        .force()
        .volumes()
        .executeBlocking();
    ```

## CreateCommand

Create a container without starting it.

```kotlin
val containerId = CreateCommand("nginx:alpine")
    .name("my-nginx")
    .port(8080, 80)
    .execute()

// Start later
StartCommand(containerId.value).execute()
```

## StartCommand

Start a stopped container.

```kotlin
StartCommand("my-container")
    .attach()      // attach STDOUT/STDERR
    .interactive() // attach STDIN
    .execute()
```

## RestartCommand

Restart a container.

```kotlin
RestartCommand("my-container")
    .time(10)  // seconds to wait for stop
    .execute()
```

## KillCommand

Force stop a container.

```kotlin
KillCommand("my-container")
    .signal("SIGKILL")  // signal to send
    .execute()
```

## PauseCommand / UnpauseCommand

Pause and unpause container processes.

```kotlin
// Pause
PauseCommand("my-container").execute()

// Unpause
UnpauseCommand("my-container").execute()
```

## WaitCommand

Block until container stops and return exit code.

```kotlin
val exitCode = WaitCommand("my-container").execute()
println("Container exited with code: $exitCode")
```

## RenameCommand

Rename a container.

```kotlin
RenameCommand("old-name", "new-name").execute()
```

## Common Patterns

### Run and Wait

```kotlin
// Run container, wait for it to complete, get exit code
val containerId = docker.run("my-job:latest") {
    name("job-runner")
    // no detach() - runs in foreground
}

val exitCode = WaitCommand(containerId.value).execute()
```

### Run with Auto-Cleanup

```kotlin
docker.run("my-image") {
    rm()  // automatically remove when stopped
    detach()
}
```

### Graceful Shutdown

```kotlin
// Stop with 30 second grace period
docker.stop("my-container") {
    time(30)
}

// If still running, force kill
docker.rm("my-container") {
    force()
}
```
