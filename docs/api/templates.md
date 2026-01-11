# Templates API Reference

Package: `io.github.joshrotenberg.dockerkotlin.template`

## Template Interface

```kotlin
interface Template : AutoCloseable {
    val name: String
    val image: String
    val tag: String
    val containerId: ContainerId?
    val isRunning: Boolean
    
    suspend fun start(): ContainerId
    suspend fun startAndWait(): ContainerId
    suspend fun stop()
    suspend fun remove()
    suspend fun waitForReady()
    fun logs(follow: Boolean = false, tail: Int? = null): Flow<String>
    suspend fun exec(vararg command: String): ExecResult
    override fun close()
}
```

## HasConnectionString

```kotlin
interface HasConnectionString {
    fun connectionString(): String
}
```

## ExecResult

```kotlin
data class ExecResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    val success: Boolean
}
```

## TemplateConfig

```kotlin
open class TemplateConfig {
    var containerName: String?
    var namePrefix: String = "dkot"
    var pullPolicy: PullPolicy = PullPolicy.IF_NOT_PRESENT
    var waitStrategy: WaitStrategy = WaitStrategy.Running
    var waitTimeout: Duration = 60.seconds
    var waitPollInterval: Duration = 500.milliseconds
    var removeOnClose: Boolean = true
    val environment: MutableMap<String, String>
    val ports: MutableMap<Int, Int>
    val dynamicPorts: MutableList<Int>
    val volumes: MutableMap<String, String>
    val labels: MutableMap<String, String>
    var network: String?
    val networkAliases: MutableList<String>
}
```

## PullPolicy

```kotlin
enum class PullPolicy {
    ALWAYS,
    IF_NOT_PRESENT,
    NEVER
}
```

## WaitStrategy

```kotlin
sealed class WaitStrategy {
    object Running : WaitStrategy()
    data class ForPort(val port: Int) : WaitStrategy()
    data class ForHttp(
        val path: String,
        val port: Int = 80,
        val statusCode: Int = 200
    ) : WaitStrategy()
    data class ForLogMessage(val pattern: Regex) : WaitStrategy()
    data class ForCommand(val command: List<String>) : WaitStrategy()
}
```

## AbstractTemplate

```kotlin
abstract class AbstractTemplate(
    override val name: String,
    protected val executor: CommandExecutor = CommandExecutor()
) : Template
```

### Protected Methods

| Method | Description |
|--------|-------------|
| `configureRunCommand(RunCommand)` | Configure the run command |
| `checkReady(): Boolean` | Check if container is ready |
| `checkPortReady(Int): Boolean` | Check if port is ready |
| `checkHttpReady(ForHttp): Boolean` | Check HTTP endpoint |
| `checkLogReady(Regex): Boolean` | Check log message |
| `checkCommandReady(List<String>): Boolean` | Check command |

## TemplateException

```kotlin
sealed class TemplateException(message: String) : Exception {
    class WaitTimeout(name: String, timeout: Duration)
    class NotRunning(name: String)
    class StartFailed(name: String, cause: Throwable)
}
```

---

Package: `io.github.joshrotenberg.dockerkotlin.redis`

## RedisTemplate

```kotlin
class RedisTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: RedisConfig.() -> Unit = {}
) : AbstractTemplate, HasConnectionString
```

### Configuration Methods

| Method | Description |
|--------|-------------|
| `version(String)` | Redis version (default: "7-alpine") |
| `password(String)` | Redis password |
| `port(Int)` | Port mapping |
| `dynamicPort()` | Dynamic port allocation |
| `maxMemory(String)` | Memory limit |
| `maxMemoryPolicy(MaxMemoryPolicy)` | Eviction policy |
| `withPersistence(String)` | Named volume for /data |
| `withConfig(String)` | Additional Redis config |

### Instance Methods

| Method | Description |
|--------|-------------|
| `connectionString()` | Redis connection URL |
| `getMappedPort()` | Get actual host port |
| `redisCommand(vararg String)` | Execute redis-cli command |

### Java Builder

```kotlin
RedisTemplate.builder(name: String): RedisTemplateBuilder
```

## RedisConfig

```kotlin
class RedisConfig : TemplateConfig() {
    var version: String = "7-alpine"
    var password: String?
    var port: Int = 6379
    var hostPort: Int?
    var maxMemory: String?
    var maxMemoryPolicy: MaxMemoryPolicy?
    var persistenceVolume: String?
    val redisConfig: MutableList<String>
}
```

## MaxMemoryPolicy

```kotlin
enum class MaxMemoryPolicy(val value: String) {
    NOEVICTION("noeviction"),
    ALLKEYS_LRU("allkeys-lru"),
    ALLKEYS_LFU("allkeys-lfu"),
    ALLKEYS_RANDOM("allkeys-random"),
    VOLATILE_LRU("volatile-lru"),
    VOLATILE_LFU("volatile-lfu"),
    VOLATILE_RANDOM("volatile-random"),
    VOLATILE_TTL("volatile-ttl")
}
```
