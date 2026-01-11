# Testing

The `docker-kotlin-testing` module provides utilities for managing Docker containers in tests,
similar to Testcontainers but built on top of docker-kotlin.

## Installation

Add the testing module to your test dependencies:

```kotlin
dependencies {
    testImplementation("io.github.joshrotenberg:docker-kotlin-testing:0.1.0")
}
```

## ContainerGuard

`ContainerGuard` provides RAII-style lifecycle management for containers. When a guard goes
out of scope (via `use` or `close()`), the container is automatically stopped and removed.

### Basic Usage

```kotlin
ContainerGuard(RedisTemplate("test-redis") { port(6379) })
    .start()
    .use { guard ->
        // Container is running
        val url = guard.connectionString()
        // Use the container...
    }
// Container automatically stopped and removed
```

### Configuration Options

| Method | Default | Description |
|--------|---------|-------------|
| `removeOnClose(Boolean)` | `true` | Remove container when guard closes |
| `stopOnClose(Boolean)` | `true` | Stop container when guard closes |
| `keepOnFailure(Boolean)` | `false` | Keep container if test fails |
| `captureLogs(Boolean)` | `false` | Print container logs on failure |
| `reuseIfRunning(Boolean)` | `false` | Reuse existing container if running |
| `waitForReady(Boolean)` | `true` | Wait for container readiness after start |
| `stopTimeout(Duration)` | `10s` | Timeout for stop operations |

### Debugging Failed Tests

Keep containers running after test failures for debugging:

```kotlin
ContainerGuard(template)
    .keepOnFailure(true)
    .captureLogs(true)
    .start()
    .use { guard ->
        // If test fails, container stays running
        // and logs are printed to console
    }
```

### Container Reuse

Speed up local development by reusing containers between test runs:

```kotlin
ContainerGuard(template)
    .reuseIfRunning(true)
    .removeOnClose(false)
    .stopOnClose(false)
    .start()
    .use { guard ->
        // Reuses existing container if available
        // Container stays running after test
    }
```

## JUnit 5 Integration

The `DockerExtension` provides automatic container lifecycle management with JUnit 5.

### Setup

```kotlin
@ExtendWith(DockerExtension::class)
class MyIntegrationTest {
    // ...
}
```

### Class-Scoped Containers

Containers in the companion object (static) are started once before all tests
and stopped after all tests complete:

```kotlin
@ExtendWith(DockerExtension::class)
class MyIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val redis = ContainerGuard(RedisTemplate("shared-redis") { port(6379) })
    }

    @Test
    fun `test one`() {
        // Uses shared redis container
    }

    @Test
    fun `test two`() {
        // Same container instance
    }
}
```

### Test-Scoped Containers

Instance fields are started fresh before each test:

```kotlin
@ExtendWith(DockerExtension::class)
class MyIntegrationTest {

    @Container
    val redis = ContainerGuard(RedisTemplate("test-redis") { port(6379) })

    @Test
    fun `test one`() {
        // Fresh container for this test
    }

    @Test
    fun `test two`() {
        // New container instance
    }
}
```

### Java Usage

```java
@ExtendWith(DockerExtension.class)
class MyIntegrationTest {

    @Container
    static ContainerGuard<RedisTemplate> redis =
        ContainerGuard.of(new RedisTemplate.Builder("redis").port(6379).build());

    @Test
    void testWithRedis() {
        String url = redis.template().connectionString();
        // Use the container...
    }
}
```

## ContainerGuardSet

Manage multiple containers as a group:

```kotlin
ContainerGuardSet()
    .add("redis", ContainerGuard(RedisTemplate("redis") { port(6379) }))
    .add("postgres", ContainerGuard(PostgresTemplate("postgres") { port(5432) }))
    .startAll()
    .use { set ->
        val redis = set.get<RedisTemplate>("redis")
        val postgres = set.get<PostgresTemplate>("postgres")
        // Use containers...
    }
```

### Parallel Startup

Start all containers concurrently for faster test setup:

```kotlin
set.startAllParallel().use { ... }
```

### DSL Syntax

```kotlin
containerGuardSet {
    container("redis", RedisTemplate("redis") { port(6379) }) {
        keepOnFailure(true)
    }
    container("postgres", PostgresTemplate("postgres") { port(5432) })
}.startAllParallelBlocking().use { set ->
    // All containers running
}
```

## Best Practices

### 1. Use Class-Scoped Containers When Possible

Starting containers is expensive. Share containers between tests when test isolation allows:

```kotlin
companion object {
    @Container
    @JvmStatic
    val redis = ContainerGuard(RedisTemplate("shared") { port(6379) })
}

@BeforeEach
fun setup() {
    // Clean container state between tests instead of restarting
    redis.execBlocking("redis-cli", "FLUSHALL")
}
```

### 2. Use Unique Container Names

Prevent naming conflicts by using unique container names:

```kotlin
val redis = ContainerGuard(
    RedisTemplate("test-redis-${UUID.randomUUID()}") { port(6379) }
)
```

### 3. Enable Debugging Options During Development

```kotlin
val redis = ContainerGuard(template)
    .keepOnFailure(true)
    .captureLogs(true)
```

### 4. Use Container Reuse for Fast Local Development

```kotlin
val reuse = System.getenv("REUSE_CONTAINERS") == "true"

val redis = ContainerGuard(template)
    .reuseIfRunning(reuse)
    .removeOnClose(!reuse)
    .stopOnClose(!reuse)
```
