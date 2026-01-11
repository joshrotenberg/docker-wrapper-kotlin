# Configuration

docker-kotlin can be configured to customize its behavior.

## Docker Client Configuration

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.core.Docker
    import io.github.joshrotenberg.dockerkotlin.core.docker
    import kotlin.time.Duration.Companion.seconds

    // Using the DSL
    val docker = docker {
        detectPlatform = true        // Auto-detect Docker/Podman/etc
        defaultTimeout = 60.seconds  // Command timeout
        dryRun = false               // Log commands without executing
        verbose = false              // Extra logging
    }

    // Or using the constructor
    val docker2 = Docker.create {
        defaultTimeout = 120.seconds
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.core.Docker;
    import io.github.joshrotenberg.dockerkotlin.core.DockerConfig;

    Docker docker = Docker.create(config -> {
        config.setDetectPlatform(true);
        config.setDefaultTimeout(Duration.ofSeconds(60));
        config.setDryRun(false);
        config.setVerbose(false);
        return config;
    });
    ```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `detectPlatform` | Boolean | `true` | Auto-detect Docker runtime (Docker, Podman, Colima, etc.) |
| `defaultTimeout` | Duration | 30 seconds | Default timeout for command execution |
| `dryRun` | Boolean | `false` | Log commands without executing them |
| `verbose` | Boolean | `false` | Enable verbose logging |

## Platform Detection

docker-kotlin automatically detects the Docker-compatible runtime:

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.platform.PlatformInfo

val platform = PlatformInfo.detect()

println("Runtime: ${platform.runtime}")      // DOCKER, PODMAN, COLIMA, etc.
println("Version: ${platform.version}")      // 24.0.7
println("Platform: ${platform.platform}")    // LINUX, MACOS, WINDOWS
println("Socket: ${platform.socketPath}")    // /var/run/docker.sock
```

### Supported Runtimes

| Runtime | Detection | Notes |
|---------|-----------|-------|
| Docker | Default | Standard Docker installation |
| Docker Desktop | Context-based | macOS/Windows Docker Desktop |
| Podman | Binary check | Falls back if `podman` is in PATH |
| Colima | Context-based | macOS Docker via Lima |
| OrbStack | Context-based | macOS Docker alternative |
| Rancher Desktop | Context-based | Cross-platform Kubernetes |

## Command Timeouts

Set timeouts at different levels:

### Global Default

```kotlin
val docker = docker {
    defaultTimeout = 60.seconds
}
```

### Per-Command

=== "Kotlin"

    ```kotlin
    import kotlin.time.Duration.Companion.minutes

    // Using the DSL
    docker.run("slow-image:latest") {
        timeout(5.minutes)
        detach()
    }

    // Using commands directly
    RunCommand("slow-image:latest")
        .timeout(5.minutes)
        .detach()
        .execute()
    ```

=== "Java"

    ```java
    import java.time.Duration;

    RunCommand.builder("slow-image:latest")
        .timeout(Duration.ofMinutes(5))
        .detach()
        .executeBlocking();
    ```

## Dry Run Mode

Preview commands without executing them:

```kotlin
val docker = docker {
    dryRun = true
}

// This logs the command but doesn't execute it
docker.run("nginx:alpine") {
    name("web")
    port(8080, 80)
    detach()
}
// Logs: docker run --name web --publish 8080:80 --detach nginx:alpine
```

## Command Preview

Inspect the command that would be executed:

```kotlin
val cmd = RunCommand("nginx:alpine")
    .name("web")
    .port(8080, 80)
    .detach()

val preview = cmd.preview()
println(preview.commandLine)
// docker run --name web --publish 8080:80 --detach nginx:alpine

println(preview.args)
// [run, --name, web, --publish, 8080:80, --detach, nginx:alpine]
```

## Escape Hatches

For CLI options not yet mapped, use escape hatches:

```kotlin
RunCommand("nginx:alpine")
    .name("web")
    .arg("--some-new-flag")           // Single argument
    .args("--option", "value")        // Multiple arguments
    .detach()
    .execute()
```

This ensures you're never blocked by missing features.

## Logging

docker-kotlin uses SLF4J for logging. Configure your preferred backend:

### Logback Example

```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- docker-kotlin logging -->
    <logger name="io.github.joshrotenberg.dockerkotlin" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### Log Levels

| Level | What's Logged |
|-------|---------------|
| `TRACE` | Command arguments, stdout/stderr output |
| `DEBUG` | Command start/completion, container lifecycle |
| `INFO` | High-level operations |
| `WARN` | Recoverable issues |
| `ERROR` | Failures |
