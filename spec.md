# docker-kotlin

A type-safe Docker CLI wrapper for Kotlin/JVM with clean Java builder APIs and an idiomatic Kotlin DSL.

## Why This Exists

### Existing JVM Docker Libraries

| Library | Approach | Status | Limitations |
|---------|----------|--------|-------------|
| [docker-java](https://github.com/docker-java/docker-java) | Docker HTTP API | Active | Heavy deps, classpath conflicts, complex TLS/socket config |
| [gesellix/docker-client](https://github.com/gesellix/docker-client) | Docker HTTP API | Active (Nov 2025) | API-based, requires socket access, 104/122 endpoints |
| [amihaiemil/docker-java-api](https://github.com/amihaiemil/docker-java-api) | Docker HTTP API | Stale (2020) | Lightweight but abandoned |
| [batect/docker-client](https://github.com/batect/docker-client) | Embeds Go libs | Archived (2023) | Dead project, limited API surface |
| [Testcontainers](https://testcontainers.com) | Docker API (docker-java) | Active | Test-focused only, heavy, Ryuk complexity |

### The CLI Wrapper Approach (What We're Building)

**None of the above wrap the Docker CLI.** They all use the Docker HTTP API directly.

CLI wrapping offers distinct advantages:

- **Works everywhere Docker works**: Podman, Colima, OrbStack, Rancher Desktop - no socket configuration
- **Docker Compose for free**: Just call `docker compose`, no separate API integration
- **No TLS/socket headaches**: If `docker` works in your terminal, this works
- **Simpler mental model**: It does exactly what `docker <command>` does
- **Full CLI coverage**: Every flag Docker supports, we can support (via escape hatches if needed)

**Tradeoff**: Process spawning overhead. For test/dev tooling, this is negligible. For high-throughput production orchestration, use the API-based libraries.

---

## Goals

- **Full Docker CLI coverage**: Support everything the Docker CLI does through type-safe builders
- **Dual API surface**: Clean Java builder pattern + idiomatic Kotlin DSL
- **Minimal core dependencies**: Just command execution, no docker-java or heavy dependencies
- **Extensible**: Core module + optional extension modules for templates/applications
- **Modern stack**: Kotlin, Gradle (Kotlin DSL), coroutines for async

## Non-Goals (for now)

- Docker API integration (we wrap the CLI, not the API)
- Test framework integration (can be added as extension module later)
- Replacing Testcontainers (different use case, though can complement it)

---

## Architecture

```
docker-kotlin/
├── docker-kotlin-core/          # Core CLI wrapper, commands, execution
├── docker-kotlin-compose/       # Docker Compose support
├── docker-kotlin-templates/     # Base template abstractions
├── docker-kotlin-redis/         # Redis templates (single, cluster, sentinel, stack)
├── docker-kotlin-postgres/      # PostgreSQL templates
├── docker-kotlin-mysql/         # MySQL templates
└── ...                          # Future extension modules
```

---

## Core Module: `docker-kotlin-core`

### Dependencies (minimal)

- Kotlin stdlib
- Kotlin coroutines (for async execution)
- SLF4J API (logging facade only)

### Command Execution

```kotlin
// Internal execution engine
interface CommandExecutor {
    suspend fun execute(args: List<String>, timeout: Duration? = null): CommandOutput
    fun executeBlocking(args: List<String>, timeout: Duration? = null): CommandOutput
}

data class CommandOutput(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    val success: Boolean get() = exitCode == 0
}
```

### DockerCommand Interface

```kotlin
interface DockerCommand<T> {
    fun buildArgs(): List<String>
    suspend fun execute(): T
    fun executeBlocking(): T
    
    // Escape hatches for unmapped options
    fun arg(arg: String): DockerCommand<T>
    fun args(vararg args: String): DockerCommand<T>
    
    // Timeout configuration
    fun timeout(duration: Duration): DockerCommand<T>
}
```

### Container Lifecycle Commands

#### RunCommand

**Java Builder API:**
```java
String containerId = RunCommand.builder("nginx:alpine")
    .name("web-server")
    .port(8080, 80)
    .env("WORKER_PROCESSES", "4")
    .volume("/data", "/app/data")
    .memory("1g")
    .cpus("0.5")
    .detach()
    .rm()
    .build()
    .executeBlocking();
```

**Kotlin DSL:**
```kotlin
val containerId = docker.run("nginx:alpine") {
    name("web-server")
    port(8080 to 80)
    env("WORKER_PROCESSES" to "4")
    volume("/data" to "/app/data")
    memory("1g")
    cpus("0.5")
    detach()
    rm()
}
```

#### Full Command List (Core)

**Container Lifecycle:**
- `RunCommand` - Create and run container
- `CreateCommand` - Create container without starting
- `StartCommand` - Start stopped container
- `StopCommand` - Stop running container
- `RestartCommand` - Restart container
- `KillCommand` - Force stop container
- `RmCommand` - Remove container
- `PauseCommand` - Pause container
- `UnpauseCommand` - Unpause container
- `WaitCommand` - Wait for container to stop
- `RenameCommand` - Rename container

**Container Inspection:**
- `PsCommand` - List containers
- `LogsCommand` - View container logs
- `InspectCommand` - Get container details (JSON)
- `TopCommand` - Show running processes
- `StatsCommand` - Live resource usage
- `PortCommand` - Show port mappings
- `DiffCommand` - Filesystem changes

**Container Operations:**
- `ExecCommand` - Execute command in container
- `AttachCommand` - Attach to container I/O
- `CpCommand` - Copy files to/from container
- `CommitCommand` - Create image from container
- `ExportCommand` - Export container filesystem

**Image Management:**
- `ImagesCommand` - List images
- `PullCommand` - Pull image from registry
- `PushCommand` - Push image to registry
- `BuildCommand` - Build image from Dockerfile
- `TagCommand` - Tag an image
- `RmiCommand` - Remove image
- `SaveCommand` - Save images to tar
- `LoadCommand` - Load images from tar
- `HistoryCommand` - Show image history
- `SearchCommand` - Search Docker registry

**Network Management:**
- `NetworkCreateCommand`
- `NetworkLsCommand`
- `NetworkInspectCommand`
- `NetworkConnectCommand`
- `NetworkDisconnectCommand`
- `NetworkRmCommand`
- `NetworkPruneCommand`

**Volume Management:**
- `VolumeCreateCommand`
- `VolumeLsCommand`
- `VolumeInspectCommand`
- `VolumeRmCommand`
- `VolumePruneCommand`

**System Commands:**
- `VersionCommand` - Docker version info
- `InfoCommand` - System information
- `EventsCommand` - Stream Docker events
- `SystemDfCommand` - Disk usage
- `SystemPruneCommand` - Clean up resources
- `LoginCommand` / `LogoutCommand` - Registry auth

**Context Commands:**
- `ContextCreateCommand`
- `ContextLsCommand`
- `ContextInspectCommand`
- `ContextUseCommand`
- `ContextRmCommand`

### Streaming Output

```kotlin
interface StreamableCommand<T> : DockerCommand<T> {
    suspend fun stream(handler: (OutputLine) -> Unit): StreamResult
    fun streamChannel(): ReceiveChannel<OutputLine>
}

sealed class OutputLine {
    data class Stdout(val line: String) : OutputLine()
    data class Stderr(val line: String) : OutputLine()
}
```

**Usage:**
```kotlin
docker.build(".") {
    tag("my-app:latest")
}.stream { line ->
    println(line)
}
```

### Platform Detection

```kotlin
enum class Runtime {
    DOCKER,
    PODMAN,
    COLIMA,
    ORBSTACK,
    RANCHER_DESKTOP,
    DOCKER_DESKTOP
}

data class PlatformInfo(
    val runtime: Runtime,
    val version: String,
    val platform: Platform,
    val socketPath: Path
)

suspend fun detectPlatform(): PlatformInfo
```

### Error Handling

```kotlin
sealed class DockerException : Exception() {
    data class DockerNotFound(override val message: String) : DockerException()
    data class DaemonNotRunning(override val message: String) : DockerException()
    data class CommandFailed(
        val command: String,
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    ) : DockerException()
    data class Timeout(val duration: Duration) : DockerException()
    data class ContainerNotFound(val containerId: String) : DockerException()
    data class ImageNotFound(val image: String) : DockerException()
    data class InvalidConfig(override val message: String) : DockerException()
}
```

### Retry Support

```kotlin
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val backoff: BackoffStrategy = BackoffStrategy.Exponential(),
    val retryOn: (DockerException) -> Boolean = { it.isRetryable }
)

sealed class BackoffStrategy {
    data class Fixed(val delay: Duration) : BackoffStrategy()
    data class Linear(val initial: Duration, val increment: Duration) : BackoffStrategy()
    data class Exponential(
        val initial: Duration = 100.milliseconds,
        val max: Duration = 10.seconds,
        val multiplier: Double = 2.0
    ) : BackoffStrategy()
}
```

### Debug / Dry-Run Support

```kotlin
val docker = Docker {
    dryRun = true  // Log commands without executing
    verbose = true // Extra logging
}

// Inspect what would be executed
val preview = docker.run("nginx") { ... }.preview()
println(preview.commandLine) // "docker run --name web -p 8080:80 nginx"
```

---

## Compose Module: `docker-kotlin-compose`

### Dependencies

- `docker-kotlin-core`

### ComposeCommand Interface

```kotlin
interface ComposeCommand<T> : DockerCommand<T> {
    fun file(path: Path): ComposeCommand<T>
    fun file(path: String): ComposeCommand<T>
    fun projectName(name: String): ComposeCommand<T>
    fun profile(profile: String): ComposeCommand<T>
    fun envFile(path: Path): ComposeCommand<T>
}
```

### Compose Commands

- `ComposeUpCommand`
- `ComposeDownCommand`
- `ComposeStartCommand`
- `ComposeStopCommand`
- `ComposeRestartCommand`
- `ComposePsCommand`
- `ComposeLogsCommand`
- `ComposeBuildCommand`
- `ComposeExecCommand`
- `ComposeRunCommand`
- `ComposePullCommand`
- `ComposePushCommand`
- `ComposeConfigCommand`
- `ComposeKillCommand`
- `ComposePauseCommand`
- `ComposeUnpauseCommand`
- `ComposeTopCommand`
- `ComposeEventsCommand`
- `ComposePortCommand`
- `ComposeImagesCommand`
- `ComposeCpCommand`
- `ComposeScaleCommand`
- `ComposeWaitCommand`

### Usage

**Java Builder:**
```java
compose.up()
    .file("docker-compose.yml")
    .projectName("myapp")
    .detach()
    .build()
    .removeOrphans()
    .wait()
    .executeBlocking();
```

**Kotlin DSL:**
```kotlin
docker.compose {
    file("docker-compose.yml")
    projectName("myapp")
    
    up {
        detach()
        build()
        removeOrphans()
        wait()
    }
}
```

---

## Templates Module: `docker-kotlin-templates`

### Dependencies

- `docker-kotlin-core`

### Template Interface

```kotlin
interface Template : AutoCloseable {
    val name: String
    val image: String
    val tag: String
    val containerId: String?
    
    suspend fun start(): String  // Returns container ID
    suspend fun startAndWait(): String
    suspend fun stop()
    suspend fun remove()
    suspend fun isRunning(): Boolean
    suspend fun logs(follow: Boolean = false, tail: Int? = null): Flow<String>
    suspend fun exec(vararg command: String): ExecResult
    suspend fun waitForReady()
    
    override fun close()  // Stops and removes container
}

interface HasConnectionString {
    fun connectionString(): String
}
```

### Resource Management

Templates implement `AutoCloseable` for safe resource cleanup:

**Kotlin `use` pattern:**
```kotlin
RedisTemplate("my-redis").use { redis ->
    redis.startAndWait()
    // do work...
}  // automatically stopped and removed
```

**Java try-with-resources:**
```java
try (var redis = new RedisTemplate("my-redis")) {
    redis.startAndWaitBlocking();
    // do work...
}  // automatically stopped and removed
```

### Container Naming

Containers can be explicitly named or auto-generated:

```kotlin
// Explicit name
val redis = RedisTemplate("my-redis") {
    name("my-explicit-name")
}

// Auto-generated (default): "dkot-redis-a3f2b1c9"
val redis = RedisTemplate("my-redis")

// Custom prefix
val redis = RedisTemplate("my-redis") {
    namePrefix("test-")  // "test-redis-a3f2b1c9"
}
```

**Naming strategy:**
```kotlin
object ContainerNaming {
    const val DEFAULT_PREFIX = "dkot"
    
    fun generate(service: String, prefix: String = DEFAULT_PREFIX): String {
        val suffix = UUID.randomUUID().toString().take(8)
        return "$prefix-$service-$suffix"
    }
}
```

### Port Allocation

Ports can be explicitly specified or auto-allocated:

```kotlin
// Explicit port mapping
val redis = RedisTemplate("my-redis") {
    port(6379)  // host 6379 -> container 6379
}

// Dynamic port allocation (Docker assigns free host port)
val redis = RedisTemplate("my-redis") {
    dynamicPort(6379)  // random host port -> container 6379
}

// After start, get actual port
redis.startAndWait()
val actualPort = redis.getMappedPort(6379)  // e.g., 32789
```

**Port utilities:**
```kotlin
object PortAllocator {
    /**
     * Find an available port on the host.
     * Useful for pre-allocating before container start.
     */
    fun findFreePort(): Int
    
    /**
     * Find multiple available ports.
     */
    fun findFreePorts(count: Int): List<Int>
    
    /**
     * Check if a port is available.
     */
    fun isPortAvailable(port: Int): Boolean
}
```

### Image Pull Policy

Control when images are pulled:

```kotlin
enum class PullPolicy {
    ALWAYS,           // Always pull before running
    IF_NOT_PRESENT,   // Pull only if not in local cache (default)
    NEVER             // Never pull, fail if not present
}

val redis = RedisTemplate("my-redis") {
    pullPolicy(PullPolicy.ALWAYS)
}
```

**Age-based pulling:**
```kotlin
val redis = RedisTemplate("my-redis") {
    pullIfOlderThan(7.days)  // Pull if local image is older than 7 days
}
```

### Container Log Integration

Container logs can be captured to SLF4J or collected programmatically:

```kotlin
// Stream logs to SLF4J
val redis = RedisTemplate("my-redis") {
    logToSlf4j()                          // Uses template name as logger
    logToSlf4j("com.example.redis")       // Custom logger name
    logToSlf4j(LogLevel.DEBUG)            // Custom level (default: INFO)
}

// Collect logs programmatically
val redis = RedisTemplate("my-redis") {
    collectLogs()  // Buffer logs in memory
}
redis.startAndWait()
val logs: List<String> = redis.collectedLogs()

// Stream logs via Flow
redis.logs(follow = true).collect { line ->
    println(line)
}

// Tail logs
redis.logs(tail = 100).collect { line ->
    println(line)
}
```

**Log consumer interface:**
```kotlin
interface LogConsumer {
    fun accept(line: String, stream: LogStream)
}

enum class LogStream { STDOUT, STDERR }

// Built-in consumers
object LogConsumers {
    fun slf4j(loggerName: String, level: LogLevel = LogLevel.INFO): LogConsumer
    fun collect(buffer: MutableList<String>): LogConsumer
    fun print(prefix: String = ""): LogConsumer
    fun tee(vararg consumers: LogConsumer): LogConsumer
}
```

### Shutdown Hook & Cleanup

Automatic cleanup of orphaned containers via JVM shutdown hook:

```kotlin
// Global configuration
DockerKotlin.configure {
    enableShutdownHook = true          // Default: true
    shutdownTimeout = 30.seconds       // Grace period for container stop
    cleanupOnShutdown = true           // Remove containers on JVM exit
    labelPrefix = "io.github.docker-kotlin"  // Label for tracking
}

// All containers are labeled for tracking
// Label: io.github.docker-kotlin.managed=true
// Label: io.github.docker-kotlin.session={session-id}
```

**Manual cleanup:**
```kotlin
// Clean up all docker-kotlin managed containers
DockerKotlin.cleanupAll()

// Clean up containers from previous sessions (orphans)
DockerKotlin.cleanupOrphans()

// Clean up containers matching a pattern
DockerKotlin.cleanup { container ->
    container.name.startsWith("test-")
}
```

**Session-based tracking:**
```kotlin
// Each JVM run gets a unique session ID
val sessionId = DockerKotlin.sessionId  // e.g., "dkot-20240115-143022-a3f2"

// Containers are labeled with session ID
// On next run, previous session's containers can be identified as orphans
```

### TemplateBuilder

```kotlin
val custom = TemplateBuilder("my-service", "my-image:latest") {
    port(8080 to 80)
    env("DEBUG" to "true")
    volume("/data" to "/app/data")
    healthCheck {
        test("curl", "-f", "http://localhost/health")
        interval(10.seconds)
        timeout(5.seconds)
        retries(3)
    }
    autoRemove()
}
```

---

## Redis Module: `docker-kotlin-redis`

### Dependencies

- `docker-kotlin-templates`

### Templates

#### RedisTemplate

```kotlin
val redis = RedisTemplate("my-redis") {
    port(6379)
    password("secret")
    version("7.2")
    persistence("/data/redis")
    maxMemory("256mb")
    maxMemoryPolicy(MaxMemoryPolicy.ALLKEYS_LRU)
}

redis.startAndWait()
println(redis.connectionString()) // redis://:secret@localhost:6379
```

#### RedisClusterTemplate

```kotlin
val cluster = RedisClusterTemplate("my-cluster") {
    masters(3)
    replicasPerMaster(1)
    portBase(7000)
    password("secret")
    persistence("/data/cluster")
}

val info = cluster.startAndWait()
info.nodes.forEach { node ->
    println("${node.role}: ${node.host}:${node.port}")
}
```

#### RedisSentinelTemplate

```kotlin
val sentinel = RedisSentinelTemplate("my-sentinel") {
    masterName("mymaster")
    sentinels(3)
    password("secret")
    quorum(2)
}
```

#### RedisStackTemplate

```kotlin
val stack = RedisStackTemplate("my-stack") {
    port(6379)
    insightPort(8001)  // RedisInsight UI
    modules(RedisModule.JSON, RedisModule.SEARCH, RedisModule.TIMESERIES)
}
```

---

## Future Extension Modules

- `docker-kotlin-postgres` - PostgreSQL templates
- `docker-kotlin-mysql` - MySQL templates  
- `docker-kotlin-mongodb` - MongoDB templates
- `docker-kotlin-kafka` - Kafka templates
- `docker-kotlin-nginx` - Nginx templates
- `docker-kotlin-localstack` - AWS LocalStack
- `docker-kotlin-junit5` - JUnit 5 integration (`@Container` annotations, lifecycle)
- `docker-kotlin-kotest` - Kotest integration

---

## Build Configuration

### Root `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
    `java-library`
    `maven-publish`
}

allprojects {
    group = "io.github.joshrotenberg"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java-library")
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    kotlin {
        jvmToolchain(21)
    }
    
    dependencies {
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.9.0"))
    }
}
```

### Core Module `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.slf4j:slf4j-api:2.0.16")
    
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.12")
}
```

---

## API Design Principles

1. **Immutable builders**: Each builder method returns a new instance (or `this` for Java compat)
2. **Suspend by default**: All execution is suspend, with `*Blocking()` variants for Java
3. **Explicit over implicit**: No magic, clear what each method does
4. **Escape hatches**: Always provide `.arg()` / `.args()` for unmapped Docker options
5. **Typed outputs**: Each command returns appropriate typed output, not just strings
6. **Streaming support**: Long-running commands support streaming via `Flow`
7. **Timeout everywhere**: All commands support optional timeouts
8. **Kotlin DSL is sugar**: DSL builds the same objects as Java builders

---

## Testing Strategy

### Unit Tests
- Command argument building (no Docker required)
- Output parsing
- Builder validation
- DSL equivalence to builders

### Integration Tests
- Require Docker (skip if unavailable via `assumeTrue`)
- Test actual container lifecycle
- Platform detection
- Streaming output

### Test Utilities
```kotlin
@EnabledIfDockerAvailable  // Custom JUnit 5 condition
class IntegrationTest {
    @Test
    fun `can run container`() = runTest {
        val id = docker.run("alpine:latest") {
            rm()
            command("echo", "hello")
        }
        // ...
    }
}
```

---

## Documentation

- KDoc on all public APIs
- Dokka for generated docs
- README with quick start
- Examples module with runnable samples

---

## CI/CD

- GitHub Actions
- Matrix: JDK 17, 21 (maybe 17 for broader compat?)
- Docker-in-Docker for integration tests
- Publish to Maven Central via Sonatype

---

## Open Questions

1. **Naming**: `docker-kotlin` vs `kdocker` vs `docker-kt` vs something else?
2. **Java compatibility level**: Target Java 17 or 21? (17 = broader adoption, 21 = modern features)
3. **Coroutine dispatcher**: Configurable or default to `Dispatchers.IO`?
4. **JSON parsing**: Use kotlinx.serialization (adds dep) or manual parsing (more work)?
5. **Health check abstraction**: Per-template custom logic or generic polling interface?
6. **Virtual threads**: Support `Dispatchers.LOOM` for Java 21 users?
7. **Multiplatform**: Stay JVM-only or consider Kotlin/Native later? (CLI wrapping works fine native)

---

## Additional Considerations

### Wait Strategies

Generic wait strategy interface for templates (similar to Testcontainers but simpler):

```kotlin
interface WaitStrategy {
    suspend fun wait(container: ContainerState, timeout: Duration)
}

// Built-in strategies
object WaitStrategies {
    /** Wait for container to be running */
    fun running(): WaitStrategy
    
    /** Wait for TCP port to accept connections */
    fun forPort(port: Int): WaitStrategy
    
    /** Wait for HTTP endpoint to return expected status */
    fun forHttp(path: String, port: Int = 80): HttpWaitStrategy
    
    /** Wait for log message matching regex */
    fun forLogMessage(pattern: Regex): WaitStrategy
    fun forLogMessage(substring: String): WaitStrategy
    
    /** Wait for health check to pass */
    fun forHealthCheck(): WaitStrategy
    
    /** Wait for command to succeed */
    fun forCommand(vararg command: String): WaitStrategy
    
    /** Composite: wait for all strategies */
    fun forAll(vararg strategies: WaitStrategy): WaitStrategy
    
    /** Composite: wait for any strategy */
    fun forAny(vararg strategies: WaitStrategy): WaitStrategy
}

class HttpWaitStrategy : WaitStrategy {
    fun forPath(path: String): HttpWaitStrategy
    fun forPort(port: Int): HttpWaitStrategy
    fun forStatusCode(code: Int): HttpWaitStrategy
    fun forStatusCodeMatching(predicate: (Int) -> Boolean): HttpWaitStrategy
    fun withBasicAuth(user: String, password: String): HttpWaitStrategy
    fun withHeader(name: String, value: String): HttpWaitStrategy
    fun withTls(): HttpWaitStrategy
    fun withReadTimeout(timeout: Duration): HttpWaitStrategy
}
```

**Usage in templates:**
```kotlin
val redis = RedisTemplate("my-redis") {
    waitStrategy(WaitStrategies.forPort(6379))
}

val web = CustomTemplate("web", "my-app:latest") {
    waitStrategy(
        WaitStrategies.forHttp("/health")
            .forPort(8080)
            .forStatusCode(200)
            .withReadTimeout(5.seconds)
    )
}
```

### Environment Variable Handling

```kotlin
// From system environment
val redis = RedisTemplate("my-redis") {
    envFromSystem("REDIS_PASSWORD")  // Pass through from host
    envFromSystem("REDIS_PASSWORD", "DB_PASS")  // Rename: host DB_PASS -> container REDIS_PASSWORD
}

// From .env file
val redis = RedisTemplate("my-redis") {
    envFile(".env")
    envFile(".env.local", override = true)
}

// From Map
val redis = RedisTemplate("my-redis") {
    envFrom(mapOf("KEY" to "value"))
    envFrom(System.getenv())  // All host env vars
}
```

### File Mounting & Copying

```kotlin
// Mount from host
val nginx = NginxTemplate("web") {
    volume("/host/path" to "/container/path")
    volumeReadOnly("/config" to "/etc/nginx/conf.d")
}

// Copy file into container after start
nginx.startAndWait()
nginx.copyToContainer("/local/file.txt", "/container/dest.txt")
nginx.copyFromContainer("/container/file.txt", "/local/dest.txt")

// Copy from classpath resource
nginx.copyToContainer(
    resource = "config/nginx.conf",  // from classpath
    destination = "/etc/nginx/nginx.conf"
)
```

### Networking Between Containers

```kotlin
// Create a shared network
val network = docker.network.create("my-network") {
    driver("bridge")
}

// Multiple containers on same network
val redis = RedisTemplate("cache") {
    network(network)
    networkAlias("redis", "cache")  // accessible as redis or cache
}

val app = CustomTemplate("app", "my-app:latest") {
    network(network)
    env("REDIS_HOST" to "redis")  // uses network alias
}

// Cleanup
network.use {
    redis.use {
        app.use {
            // containers can communicate
        }
    }
}
```

### Container Inspection

```kotlin
// Get container details after start
val redis = RedisTemplate("my-redis")
redis.startAndWait()

val info = redis.inspect()
info.id              // Full container ID
info.name            // Container name
info.state           // running, exited, paused, etc.
info.health          // healthy, unhealthy, starting, none
info.ipAddress       // Container IP (on default network)
info.ports           // Map<Int, Int> of container -> host ports
info.mounts          // List of volume mounts
info.env             // Environment variables
info.labels          // Container labels
info.createdAt       // Instant
info.startedAt       // Instant?
info.exitCode        // Int? (if exited)
```

### Parallel Container Startup

```kotlin
// Start multiple containers in parallel
val containers = listOf(
    RedisTemplate("redis"),
    PostgresTemplate("postgres"),
    KafkaTemplate("kafka")
)

// Parallel start with structured concurrency
coroutineScope {
    containers.map { container ->
        async { container.startAndWait() }
    }.awaitAll()
}

// Or use helper
DockerKotlin.startAll(containers, parallel = true)

// With dependencies (DAG-based startup)
DockerKotlin.startWithDependencies {
    val db = add(PostgresTemplate("db"))
    val cache = add(RedisTemplate("cache"))
    val app = add(AppTemplate("app"), dependsOn = listOf(db, cache))
}
```

### Privileged Mode & Capabilities

```kotlin
val container = CustomTemplate("dind", "docker:dind") {
    privileged()  // Full privileges (use sparingly)
}

val container = CustomTemplate("network-tool", "nicolaka/netshoot") {
    capAdd("NET_ADMIN", "SYS_PTRACE")
    capDrop("MKNOD")
}
```

### Tmpfs & Memory-Backed Storage

```kotlin
val redis = RedisTemplate("my-redis") {
    tmpfs("/data", size = "100m")  // RAM-backed /data
}
```

### Init Process

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    init()  // Use tini as PID 1 for proper signal handling
}
```

### User & Working Directory

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    user("1000:1000")        // Run as specific UID:GID
    user("nobody")           // Run as user by name
    workdir("/app")          // Set working directory
}
```

### Labels & Annotations

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    label("com.example.version", "1.0.0")
    label("com.example.maintainer", "team@example.com")
    labels(mapOf(
        "environment" to "test",
        "project" to "myproject"
    ))
}
```

### Entrypoint & Command Override

```kotlin
val container = CustomTemplate("debug", "alpine") {
    entrypoint("/bin/sh", "-c")
    command("while true; do sleep 1; done")
}

// Or just command
val container = CustomTemplate("test", "alpine") {
    command("echo", "hello", "world")
}
```

### Health Check Configuration

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    healthCheck {
        command("curl", "-f", "http://localhost:8080/health")
        interval(10.seconds)
        timeout(5.seconds)
        startPeriod(30.seconds)
        retries(3)
    }
    
    // Or disable
    noHealthCheck()
}
```

### Resource Constraints

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    // Memory
    memory("512m")
    memorySwap("1g")
    memoryReservation("256m")
    
    // CPU
    cpus(1.5)                    // 1.5 CPUs
    cpuShares(512)               // Relative weight
    cpuPeriod(100_000)           // Microseconds
    cpuQuota(50_000)             // Microseconds
    cpuset("0,2")                // Pin to specific CPUs
    
    // IO
    blkioWeight(500)             // 10-1000
    
    // PIDs
    pidsLimit(100)               // Max processes
}
```

### Restart Policies

```kotlin
val container = CustomTemplate("service", "my-service:latest") {
    restart(RestartPolicy.NO)              // Default
    restart(RestartPolicy.ALWAYS)
    restart(RestartPolicy.UNLESS_STOPPED)
    restart(RestartPolicy.ON_FAILURE, maxRetries = 5)
}
```

### Stop Configuration

```kotlin
val container = CustomTemplate("app", "my-app:latest") {
    stopSignal("SIGTERM")        // Signal to send (default SIGTERM)
    stopTimeout(30.seconds)      // Grace period before SIGKILL
}
```

---

## Appendix: Docker CLI Reference

For complete flag coverage, reference:
- `docker run --help`
- `docker compose --help`
- https://docs.docker.com/reference/cli/docker/

The Rust implementation in `docker-wrapper` covers 80+ commands with comprehensive flag support - use as reference for parity.

---

## Appendix: Comparison with Testcontainers

| Feature | docker-kotlin | Testcontainers |
|---------|--------------|----------------|
| Docker communication | CLI wrapper | Docker API |
| Compose support | Native (full CLI) | Limited wrapper |
| Podman/Colima/OrbStack | Works out of box | Requires config |
| JUnit integration | Optional module | Core feature |
| Ryuk (cleanup container) | No (JVM hooks) | Yes |
| Startup overhead | Process spawn | API call |
| Coverage | Full CLI | Subset of API |
| Primary use case | Testing + dev tooling | Testing only |
| Async model | Coroutines | Blocking |
| Kotlin DSL | First-class | N/A |

---

## Appendix: Migration from Testcontainers

```kotlin
// Testcontainers
@Container
val redis = GenericContainer("redis:7")
    .withExposedPorts(6379)
    .waitingFor(Wait.forListeningPort())

// docker-kotlin
val redis = RedisTemplate("redis") {
    version("7")
    dynamicPort(6379)
    waitStrategy(WaitStrategies.forPort(6379))
}

// Testcontainers
val pg = PostgreSQLContainer("postgres:15")
    .withDatabaseName("test")
    .withUsername("user")
    .withPassword("pass")

// docker-kotlin
val pg = PostgresTemplate("pg") {
    version("15")
    database("test")
    user("user")
    password("pass")
}
```
