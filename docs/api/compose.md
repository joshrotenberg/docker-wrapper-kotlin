# Compose API Reference

Package: `io.github.joshrotenberg.dockerkotlin.compose`

## ComposeConfig

Base configuration for compose commands.

```kotlin
open class ComposeConfig {
    val files: MutableList<Path>
    var projectName: String?
    var projectDirectory: Path?
    val profiles: MutableList<String>
    var envFile: Path?
    var compatibility: Boolean
    var dryRun: Boolean
    var progress: ProgressType?
}
```

## ProgressType

```kotlin
enum class ProgressType(val value: String) {
    AUTO("auto"),
    TTY("tty"),
    PLAIN("plain"),
    JSON("json"),
    QUIET("quiet")
}
```

## AbstractComposeCommand

Base class for compose commands.

```kotlin
abstract class AbstractComposeCommand<T> : AbstractDockerCommand<T>()
```

### Common Methods

| Method | Description |
|--------|-------------|
| `file(Path)` | Add compose file |
| `file(String)` | Add compose file by path string |
| `projectName(String)` | Set project name |
| `projectDirectory(Path)` | Set project directory |
| `profile(String)` | Add profile |
| `envFile(Path)` | Set environment file |
| `compatibility()` | Enable compatibility mode |
| `dryRun()` | Enable dry run mode |
| `progress(ProgressType)` | Set progress output type |

## ComposeUpCommand

```kotlin
class ComposeUpCommand(executor: CommandExecutor = CommandExecutor())
```

### Methods

| Method | Description |
|--------|-------------|
| `detach()` | Run in background |
| `build()` | Build before starting |
| `forceRecreate()` | Force recreate containers |
| `noRecreate()` | Don't recreate |
| `noBuild()` | Don't build |
| `noStart()` | Create without starting |
| `removeOrphans()` | Remove orphaned containers |
| `wait()` | Wait for healthy |
| `timeout(Int)` | Shutdown timeout in seconds |
| `services(vararg String)` | Specific services |
| `scale(String, Int)` | Scale service |

## ComposeDownCommand

```kotlin
class ComposeDownCommand(executor: CommandExecutor = CommandExecutor())
```

### Methods

| Method | Description |
|--------|-------------|
| `removeOrphans()` | Remove orphaned containers |
| `volumes()` | Remove volumes |
| `rmi(RemoveImages)` | Remove images |
| `timeout(Int)` | Shutdown timeout |

## RemoveImages

```kotlin
enum class RemoveImages(val value: String) {
    ALL("all"),
    LOCAL("local")
}
```

## ComposePsCommand

```kotlin
class ComposePsCommand(executor: CommandExecutor = CommandExecutor())
```

### Methods

| Method | Description |
|--------|-------------|
| `all()` | Include stopped containers |
| `quiet()` | Only show IDs |
| `format(String)` | Output format |
| `status(ServiceStatus)` | Filter by status |
| `services(vararg String)` | Specific services |

### Return Type

```kotlin
data class ComposeServiceInfo(
    val name: String,
    val service: String,
    val status: String,
    val ports: String
)
```

## ServiceStatus

```kotlin
enum class ServiceStatus(val value: String) {
    PAUSED("paused"),
    RESTARTING("restarting"),
    REMOVING("removing"),
    RUNNING("running"),
    DEAD("dead"),
    CREATED("created"),
    EXITED("exited")
}
```
