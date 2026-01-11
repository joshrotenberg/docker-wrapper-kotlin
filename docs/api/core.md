# Core API Reference

Package: `io.github.joshrotenberg.dockerkotlin.core`

## Docker

Main entry point for Docker operations.

```kotlin
class Docker(config: DockerConfig = DockerConfig())
```

### Factory Methods

| Method | Description |
|--------|-------------|
| `Docker.create()` | Create with defaults |
| `Docker.create { ... }` | Create with configuration |
| `docker { ... }` | Kotlin DSL factory |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `version()` | `VersionInfo` | Get Docker version (suspend) |
| `versionBlocking()` | `VersionInfo` | Get Docker version (blocking) |
| `run(image) { ... }` | `ContainerId` | Run container (suspend) |
| `runBlocking(image) { ... }` | `ContainerId` | Run container (blocking) |
| `stop(container) { ... }` | `Unit` | Stop container (suspend) |
| `stopBlocking(container) { ... }` | `Unit` | Stop container (blocking) |
| `rm(container) { ... }` | `Unit` | Remove container (suspend) |
| `rmBlocking(container) { ... }` | `Unit` | Remove container (blocking) |
| `pull(image) { ... }` | `Unit` | Pull image (suspend) |
| `pullBlocking(image) { ... }` | `Unit` | Pull image (blocking) |

## DockerConfig

Configuration for the Docker client.

```kotlin
class DockerConfig {
    var detectPlatform: Boolean = true
    var defaultTimeout: Duration = 30.seconds
    var dryRun: Boolean = false
    var verbose: Boolean = false
}
```

## CommandExecutor

Executes Docker CLI commands.

```kotlin
class CommandExecutor(
    platformInfo: PlatformInfo? = null,
    defaultTimeout: Duration = DEFAULT_TIMEOUT
)
```

### Methods

| Method | Description |
|--------|-------------|
| `execute(args, timeout)` | Execute command (suspend) |
| `executeBlocking(args, timeout)` | Execute command (blocking) |

## CommandOutput

Result of command execution.

```kotlin
data class CommandOutput(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    val success: Boolean
    fun stdoutLines(): List<String>
    fun stderrLines(): List<String>
}
```

## ContainerId

Container identifier returned from run commands.

```kotlin
@JvmInline
value class ContainerId(val value: String) {
    fun short(): String  // First 12 characters
}
```

## DockerException

Sealed exception hierarchy.

```kotlin
sealed class DockerException : Exception {
    class DockerNotFound(message: String)
    class DaemonNotRunning(message: String)
    class CommandFailed(command: String, exitCode: Int, stdout: String, stderr: String)
    class Timeout(duration: Duration)
    class ContainerNotFound(containerId: String)
    class ImageNotFound(image: String)
    class NetworkNotFound(network: String)
    class VolumeNotFound(volume: String)
    class InvalidConfig(message: String)
    class Generic(message: String, cause: Throwable?)
}
```

## Platform Detection

### Runtime

```kotlin
enum class Runtime(val command: String) {
    DOCKER("docker"),
    PODMAN("podman"),
    COLIMA("docker"),
    ORBSTACK("docker"),
    RANCHER_DESKTOP("docker"),
    DOCKER_DESKTOP("docker")
}
```

### Platform

```kotlin
enum class Platform {
    LINUX, MACOS, WINDOWS, UNKNOWN
}
```

### PlatformInfo

```kotlin
data class PlatformInfo(
    val runtime: Runtime,
    val version: String,
    val platform: Platform,
    val socketPath: Path?,
    val environmentVars: Map<String, String>
) {
    companion object {
        fun detect(): PlatformInfo
    }
}
```

## Commands

### DockerCommand Interface

```kotlin
interface DockerCommand<T> {
    fun buildArgs(): List<String>
    suspend fun execute(): T
    fun executeBlocking(): T
    fun arg(arg: String): DockerCommand<T>
    fun args(vararg args: String): DockerCommand<T>
    fun timeout(duration: Duration): DockerCommand<T>
    fun preview(): CommandPreview
}
```

### CommandPreview

```kotlin
data class CommandPreview(
    val commandLine: String,
    val args: List<String>
)
```

### RunCommand

```kotlin
class RunCommand(image: String, executor: CommandExecutor = CommandExecutor())
```

Key methods:
- `name(String)`, `hostname(String)`
- `port(host, container)`, `dynamicPort(container)`
- `env(key, value)`, `env(Map)`
- `volume(host, container)`, `namedVolume(name, container)`
- `label(key, value)`, `labels(Map)`
- `network(String)`, `networkAlias(String)`
- `memory(String)`, `cpus(String)`, `cpuShares(Int)`
- `restart(RestartPolicy)`
- `detach()`, `rm()`, `init()`, `privileged()`
- `user(String)`, `workdir(String)`
- `entrypoint(String)`, `command(vararg String)`
- `platform(String)`

### RestartPolicy

```kotlin
sealed class RestartPolicy(val value: String) {
    object No : RestartPolicy("no")
    object Always : RestartPolicy("always")
    object UnlessStopped : RestartPolicy("unless-stopped")
    class OnFailure(maxRetries: Int? = null) : RestartPolicy
}
```

### Protocol

```kotlin
enum class Protocol(val value: String) {
    TCP("tcp"),
    UDP("udp")
}
```

### VersionInfo

```kotlin
data class VersionInfo(
    val clientVersion: String,
    val serverVersion: String?,
    val apiVersion: String?,
    val goVersion: String?,
    val os: String?,
    val arch: String?
)
```
