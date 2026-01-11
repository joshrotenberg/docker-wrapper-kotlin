# Wait Strategies

Wait strategies determine when a container is considered "ready" for use.

## Overview

When you call `startAndWait()`, the template polls the container using the configured wait strategy until it succeeds or times out.

```kotlin
RedisTemplate("my-redis") {
    waitStrategy = WaitStrategy.ForPort(6379)
    waitTimeout = 60.seconds
    waitPollInterval = 500.milliseconds
}.use { redis ->
    redis.startAndWait()  // Blocks until ready
}
```

## Available Strategies

### Running

Wait for the container to be in "running" state. This is the simplest strategy but doesn't guarantee the service is ready.

```kotlin
waitStrategy = WaitStrategy.Running
```

### ForPort

Wait for a TCP port to accept connections.

```kotlin
waitStrategy = WaitStrategy.ForPort(8080)
```

This is the most common strategy and works for most services.

### ForHttp

Wait for an HTTP endpoint to return an expected status code.

```kotlin
waitStrategy = WaitStrategy.ForHttp(
    path = "/health",
    port = 8080,
    statusCode = 200
)
```

Useful for services with health check endpoints.

### ForLogMessage

Wait for a specific message to appear in container logs.

```kotlin
// Match exact substring
waitStrategy = WaitStrategy.ForLogMessage("Server started")

// Match regex pattern
waitStrategy = WaitStrategy.ForLogMessage(Regex("Ready to accept connections.*"))
```

Useful when the service logs a specific message on startup.

### ForCommand

Wait for a command to succeed inside the container.

```kotlin
// Simple command
waitStrategy = WaitStrategy.ForCommand("redis-cli", "ping")

// Command with expected output
waitStrategy = WaitStrategy.ForCommand(listOf("pg_isready", "-U", "postgres"))
```

Useful for database readiness checks.

## Configuration

### Timeout

Maximum time to wait for the container to be ready:

```kotlin
RedisTemplate("my-redis") {
    waitTimeout = 120.seconds  // default: 60 seconds
}
```

If the timeout is exceeded, a `TemplateException.WaitTimeout` is thrown.

### Poll Interval

How often to check the container status:

```kotlin
RedisTemplate("my-redis") {
    waitPollInterval = 1.seconds  // default: 500 milliseconds
}
```

## Template Defaults

Each template provides a sensible default wait strategy:

| Template | Default Strategy |
|----------|-----------------|
| `RedisTemplate` | `ForCommand("redis-cli", "ping")` |
| `PostgresTemplate` | `ForCommand("pg_isready", "-U", "postgres")` |
| `MysqlTemplate` | `ForCommand("mysqladmin", "ping")` |
| `MongodbTemplate` | `ForCommand("mongosh", "--eval", "db.runCommand('ping')")` |
| `NginxTemplate` | `ForPort(80)` |

## Custom Wait Logic

For complex scenarios, override `checkReady()` in your template:

```kotlin
class MyTemplate(...) : AbstractTemplate(...) {
    
    override suspend fun checkReady(): Boolean {
        // Custom readiness check
        val result = exec("my-health-check")
        if (!result.success) return false
        
        // Parse output
        val status = parseStatus(result.stdout)
        return status == "healthy"
    }
}
```

## Examples

### Database with Authentication

```kotlin
PostgresTemplate("my-db") {
    password("secret")
    waitStrategy = WaitStrategy.ForCommand(
        "pg_isready", "-U", "postgres", "-d", "mydb"
    )
    waitTimeout = 120.seconds
}
```

### Web Service with Health Endpoint

```kotlin
MyWebService("my-service") {
    waitStrategy = WaitStrategy.ForHttp(
        path = "/api/health",
        port = 8080,
        statusCode = 200
    )
}
```

### Service with Startup Log

```kotlin
KafkaTemplate("my-kafka") {
    waitStrategy = WaitStrategy.ForLogMessage(
        Regex(".*Kafka Server.*started.*")
    )
    waitTimeout = 180.seconds
}
```

### Multiple Conditions

For complex scenarios, implement custom logic:

```kotlin
override suspend fun checkReady(): Boolean {
    // Check port first
    if (!checkPortReady(8080)) return false
    
    // Then check health endpoint
    val health = exec("curl", "-sf", "http://localhost:8080/health")
    if (!health.success) return false
    
    // Parse health response
    return health.stdout.contains("\"status\":\"up\"")
}
```

## Troubleshooting

### Timeout Errors

If containers frequently timeout:

1. Increase `waitTimeout`:
   ```kotlin
   waitTimeout = 180.seconds
   ```

2. Check container logs:
   ```kotlin
   redis.start()
   redis.logs().collect { println(it) }
   ```

3. Use a more appropriate strategy:
   ```kotlin
   // Instead of ForPort, use ForCommand for databases
   waitStrategy = WaitStrategy.ForCommand("pg_isready")
   ```

### False Positives

If containers report ready but aren't:

1. Use a more specific strategy:
   ```kotlin
   // Instead of ForPort
   waitStrategy = WaitStrategy.ForHttp("/health", 8080, 200)
   ```

2. Add a startup delay in your container:
   ```kotlin
   configureRunCommand { cmd ->
       cmd.env("STARTUP_DELAY", "5")
   }
   ```
