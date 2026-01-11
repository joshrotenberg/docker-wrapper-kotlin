# Compose Commands

Detailed documentation for Docker Compose commands.

## ComposeUpCommand

Create and start containers.

=== "Kotlin"

    ```kotlin
    ComposeUpCommand()
        .file("docker-compose.yml")
        .projectName("myapp")
        
        // Startup options
        .detach()              // run in background
        .build()               // build images before starting
        .forceRecreate()       // recreate even if unchanged
        .noRecreate()          // don't recreate if running
        .noBuild()             // don't build images
        .noStart()             // create but don't start
        
        // Cleanup
        .removeOrphans()       // remove containers for undefined services
        
        // Wait for healthy
        .wait()                // wait for services to be healthy
        .timeout(60)           // timeout in seconds
        
        // Scaling
        .scale("web", 3)       // run 3 instances of web
        .scale("worker", 5)
        
        // Specific services
        .services("web", "db") // only these services
        
        .execute()
    ```

=== "Java"

    ```java
    ComposeUpCommand.builder()
        .file("docker-compose.yml")
        .projectName("myapp")
        .detach()
        .build()
        .wait()
        .executeBlocking();
    ```

## ComposeDownCommand

Stop and remove containers, networks, and optionally volumes and images.

=== "Kotlin"

    ```kotlin
    ComposeDownCommand()
        .file("docker-compose.yml")
        
        // Cleanup options
        .removeOrphans()           // remove orphaned containers
        .volumes()                 // remove named volumes
        .rmi(RemoveImages.ALL)     // remove all images
        .rmi(RemoveImages.LOCAL)   // remove only locally built images
        
        // Timeout
        .timeout(30)               // seconds to wait for stop
        
        .execute()
    ```

=== "Java"

    ```java
    ComposeDownCommand.builder()
        .file("docker-compose.yml")
        .volumes()
        .removeOrphans()
        .executeBlocking();
    ```

## ComposePsCommand

List containers.

=== "Kotlin"

    ```kotlin
    val services = ComposePsCommand()
        .file("docker-compose.yml")
        
        // Filter options
        .all()                          // include stopped
        .status(ServiceStatus.RUNNING)  // filter by status
        .services("web", "db")          // specific services
        
        // Output options
        .quiet()                        // only IDs
        
        .execute()

    services.forEach { service ->
        println("${service.name}: ${service.status} - ${service.ports}")
    }
    ```

=== "Java"

    ```java
    List<ComposeServiceInfo> services = ComposePsCommand.builder()
        .file("docker-compose.yml")
        .all()
        .executeBlocking();
    ```

### Service Status Filters

```kotlin
ServiceStatus.PAUSED
ServiceStatus.RESTARTING
ServiceStatus.REMOVING
ServiceStatus.RUNNING
ServiceStatus.DEAD
ServiceStatus.CREATED
ServiceStatus.EXITED
```

## ComposeLogsCommand

View output from containers.

```kotlin
ComposeLogsCommand()
    .file("docker-compose.yml")
    
    // Output options
    .follow()              // stream logs
    .timestamps()          // show timestamps
    .tail(100)             // last N lines
    .since("2024-01-01")   // logs since timestamp
    .until("2024-01-02")   // logs until timestamp
    
    // Filter
    .noColor()             // no ANSI colors
    .noLogPrefix()         // no service prefix
    
    // Specific services
    .services("web", "db")
    
    .execute()
```

## ComposeExecCommand

Execute a command in a running container.

```kotlin
val output = ComposeExecCommand("web", "ls", "-la")
    .file("docker-compose.yml")
    
    // Options
    .user("www-data")      // run as user
    .workdir("/app")       // working directory
    .env("DEBUG", "true")  // environment variable
    .privileged()          // privileged mode
    .index(2)              // container index if scaled
    
    // TTY options
    .interactive()
    .tty()
    .noTty()
    
    .execute()

println(output.stdout)
```

## ComposeRunCommand

Run a one-off command.

```kotlin
ComposeRunCommand("web", "npm", "test")
    .file("docker-compose.yml")
    
    // Options
    .rm()                  // remove after run
    .noDeps()              // don't start dependencies
    .user("node")
    .workdir("/app")
    .env("CI", "true")
    
    // Port options
    .publish(3000, 3000)
    .servicePorts()        // use service's port mappings
    
    // Volume options
    .volume("/host", "/container")
    
    .execute()
```

## ComposeBuildCommand

Build or rebuild services.

```kotlin
ComposeBuildCommand()
    .file("docker-compose.yml")
    
    // Build options
    .noCache()             // don't use cache
    .pull()                // always pull base images
    .parallel()            // build in parallel
    .quiet()               // suppress output
    
    // Build args
    .buildArg("VERSION", "1.0")
    .buildArg("DEBUG", "false")
    
    // Specific services
    .services("web", "api")
    
    .execute()
```

## ComposePullCommand

Pull service images.

```kotlin
ComposePullCommand()
    .file("docker-compose.yml")
    
    .ignorePullFailures()  // don't fail on pull error
    .includeDeps()         // also pull dependencies
    .parallel()            // pull in parallel
    .quiet()
    
    .services("web", "db")
    
    .execute()
```

## ComposePushCommand

Push service images.

```kotlin
ComposePushCommand()
    .file("docker-compose.yml")
    
    .ignorePushFailures()
    
    .services("web", "api")
    
    .execute()
```

## ComposeConfigCommand

Validate and view the compose configuration.

```kotlin
val config = ComposeConfigCommand()
    .file("docker-compose.yml")
    .file("docker-compose.override.yml")
    
    // Output options
    .quiet()               // only validate
    .services()            // list service names
    .volumes()             // list volume names
    .images()              // list image names
    
    .execute()

println(config)  // merged YAML output
```

## ComposeScaleCommand

Scale services.

```kotlin
ComposeScaleCommand()
    .file("docker-compose.yml")
    
    .scale("web", 5)
    .scale("worker", 10)
    
    .noDeps()              // don't scale dependencies
    
    .execute()
```

## ComposePortCommand

Print the public port for a port binding.

```kotlin
val hostPort = ComposePortCommand("web", 80)
    .file("docker-compose.yml")
    
    .protocol("tcp")       // tcp or udp
    .index(1)              // container index if scaled
    
    .execute()

println("Web service available at localhost:$hostPort")
```

## Common Patterns

### Development Workflow

```kotlin
// Start development environment
ComposeUpCommand()
    .file("docker-compose.yml")
    .file("docker-compose.dev.yml")
    .detach()
    .build()
    .execute()

// Watch logs
ComposeLogsCommand()
    .file("docker-compose.yml")
    .follow()
    .execute()
```

### CI/CD Pipeline

```kotlin
// Build images
ComposeBuildCommand()
    .file("docker-compose.yml")
    .noCache()
    .pull()
    .execute()

// Run tests
ComposeRunCommand("app", "npm", "test")
    .file("docker-compose.yml")
    .rm()
    .execute()

// Push images
ComposePushCommand()
    .file("docker-compose.yml")
    .execute()
```

### Scaling Services

```kotlin
// Scale up
ComposeUpCommand()
    .file("docker-compose.yml")
    .scale("web", 5)
    .scale("worker", 3)
    .detach()
    .execute()

// Check status
ComposePsCommand()
    .file("docker-compose.yml")
    .execute()

// Scale down
ComposeScaleCommand()
    .file("docker-compose.yml")
    .scale("web", 1)
    .scale("worker", 1)
    .execute()
```

### Complete Cleanup

```kotlin
ComposeDownCommand()
    .file("docker-compose.yml")
    .volumes()           // remove volumes
    .rmi(RemoveImages.ALL)  // remove images
    .removeOrphans()
    .execute()
```
