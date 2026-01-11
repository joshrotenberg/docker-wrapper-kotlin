# Templates Overview

The `docker-kotlin-templates` module provides pre-configured container templates for common services.

## Why Templates?

Templates offer:

- **Sensible defaults** - Pre-configured for typical use cases
- **Type-safe configuration** - Compile-time validation
- **Lifecycle management** - Start, stop, cleanup with `AutoCloseable`
- **Wait strategies** - Automatic readiness detection
- **Connection strings** - Easy integration with your application

## Installation

=== "Kotlin DSL"

    ```kotlin
    dependencies {
        // Base template module
        implementation("io.github.joshrotenberg:docker-kotlin-templates:0.1.0")
        
        // Specific templates
        implementation("io.github.joshrotenberg:docker-kotlin-redis:0.1.0")
        // implementation("io.github.joshrotenberg:docker-kotlin-postgres:0.1.0")
        // implementation("io.github.joshrotenberg:docker-kotlin-mysql:0.1.0")
    }
    ```

=== "Groovy DSL"

    ```groovy
    dependencies {
        implementation 'io.github.joshrotenberg:docker-kotlin-templates:0.1.0'
        implementation 'io.github.joshrotenberg:docker-kotlin-redis:0.1.0'
    }
    ```

## Quick Start

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate

    // Use the template with automatic cleanup
    RedisTemplate("my-redis") {
        port(6379)
        password("secret")
    }.use { redis ->
        redis.startAndWait()
        
        println("Connection: ${redis.connectionString()}")
        // redis://:secret@localhost:6379
        
        // Your application code here
        
    } // Container automatically stopped and removed
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate;

    try (RedisTemplate redis = RedisTemplate.builder("my-redis")
            .port(6379)
            .password("secret")
            .build()) {
        
        redis.startAndWaitBlocking();
        
        System.out.println("Connection: " + redis.connectionString());
        
        // Your application code here
        
    } // Container automatically stopped and removed
    ```

## Template Lifecycle

### Starting Containers

```kotlin
val redis = RedisTemplate("my-redis") {
    port(6379)
}

// Start and return immediately
val containerId = redis.start()

// Start and wait for ready
val containerId = redis.startAndWait()
```

### Stopping Containers

```kotlin
// Stop the container
redis.stop()

// Remove the container
redis.remove()

// Stop and remove (via AutoCloseable)
redis.close()
```

### Using AutoCloseable

The recommended pattern uses Kotlin's `use` or Java's try-with-resources:

=== "Kotlin"

    ```kotlin
    RedisTemplate("test-redis") {
        port(6379)
    }.use { redis ->
        redis.startAndWait()
        // Use redis...
    } // Automatically stopped and removed
    ```

=== "Java"

    ```java
    try (RedisTemplate redis = RedisTemplate.builder("test-redis")
            .port(6379)
            .build()) {
        redis.startAndWaitBlocking();
        // Use redis...
    } // Automatically stopped and removed
    ```

## Common Configuration

All templates support these options:

```kotlin
SomeTemplate("my-service") {
    // Container naming
    containerName = "explicit-name"    // or auto-generated
    namePrefix = "test"                // prefix for auto names
    
    // Port configuration
    ports[8080] = 80                   // host:container
    dynamicPorts.add(3000)             // auto-assign host port
    
    // Environment
    environment["KEY"] = "value"
    
    // Volumes
    volumes["/host/path"] = "/container/path"
    
    // Labels
    labels["app"] = "myapp"
    labels["env"] = "test"
    
    // Networking
    network = "my-network"
    networkAliases.add("alias1")
    
    // Readiness
    waitStrategy = WaitStrategy.ForPort(8080)
    waitTimeout = 60.seconds
    waitPollInterval = 500.milliseconds
    
    // Cleanup
    removeOnClose = true               // remove on close() (default)
    
    // Image
    pullPolicy = PullPolicy.IF_NOT_PRESENT
}
```

## Available Templates

### Redis

```kotlin
import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate

RedisTemplate("my-redis") {
    version("7-alpine")
    port(6379)
    password("secret")
    maxMemory("256mb")
    maxMemoryPolicy(MaxMemoryPolicy.ALLKEYS_LRU)
    withPersistence("redis-data")
}
```

### Coming Soon

- `PostgresTemplate` - PostgreSQL database
- `MysqlTemplate` - MySQL database
- `MongodbTemplate` - MongoDB
- `KafkaTemplate` - Apache Kafka
- `NginxTemplate` - Nginx web server

## Creating Custom Templates

Extend `AbstractTemplate` to create your own:

```kotlin
class MyServiceTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: MyServiceConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = MyServiceConfig().apply(configure)
    
    override val image = "my-service"
    override val tag get() = config.version

    override fun configureRunCommand(cmd: RunCommand) {
        cmd.port(config.port, 8080)
        cmd.env("CONFIG_KEY", config.value)
    }

    override fun connectionString(): String {
        return "myservice://localhost:${config.port}"
    }
}

class MyServiceConfig : TemplateConfig() {
    var version: String = "latest"
    var port: Int = 8080
    var value: String = "default"
}
```

## Next Steps

- [Wait Strategies](wait-strategies.md) - Configure readiness detection
- [Redis Templates](redis.md) - Redis-specific documentation
