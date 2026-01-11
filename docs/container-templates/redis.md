# Redis Templates

The `docker-kotlin-redis` module provides pre-configured Redis container templates.

## Installation

=== "Kotlin DSL"

    ```kotlin
    dependencies {
        implementation("io.github.joshrotenberg:docker-kotlin-redis:0.1.0")
    }
    ```

=== "Groovy DSL"

    ```groovy
    dependencies {
        implementation 'io.github.joshrotenberg:docker-kotlin-redis:0.1.0'
    }
    ```

## RedisTemplate

Basic Redis server template.

### Quick Start

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate

    RedisTemplate("my-redis") {
        port(6379)
    }.use { redis ->
        redis.startAndWait()
        
        println("Redis ready at: ${redis.connectionString()}")
        // redis://localhost:6379
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate;

    try (RedisTemplate redis = RedisTemplate.builder("my-redis")
            .port(6379)
            .build()) {
        redis.startAndWaitBlocking();
        System.out.println("Redis ready at: " + redis.connectionString());
    }
    ```

### Configuration

```kotlin
RedisTemplate("my-redis") {
    // Version
    version("7-alpine")      // Redis version/tag (default: 7-alpine)
    
    // Port
    port(6379)               // explicit port mapping
    dynamicPort()            // let Docker assign port
    
    // Authentication
    password("secret")       // requirepass
    
    // Memory
    maxMemory("256mb")       // maxmemory
    maxMemoryPolicy(MaxMemoryPolicy.ALLKEYS_LRU)
    
    // Persistence
    withPersistence("redis-data")  // named volume for /data
    
    // Custom Redis config
    withConfig("--appendonly yes")
    withConfig("--save 60 1000")
}
```

### Memory Policies

```kotlin
MaxMemoryPolicy.NOEVICTION      // return error on memory limit
MaxMemoryPolicy.ALLKEYS_LRU     // evict any key using LRU
MaxMemoryPolicy.ALLKEYS_LFU     // evict any key using LFU
MaxMemoryPolicy.ALLKEYS_RANDOM  // evict any key randomly
MaxMemoryPolicy.VOLATILE_LRU    // evict keys with TTL using LRU
MaxMemoryPolicy.VOLATILE_LFU    // evict keys with TTL using LFU
MaxMemoryPolicy.VOLATILE_RANDOM // evict keys with TTL randomly
MaxMemoryPolicy.VOLATILE_TTL    // evict keys with nearest TTL
```

### Connection String

```kotlin
val redis = RedisTemplate("my-redis") {
    port(6379)
    password("secret")
}

redis.startAndWait()

println(redis.connectionString())
// redis://:secret@localhost:6379
```

### Executing Redis Commands

```kotlin
redis.startAndWait()

// Execute Redis CLI commands
val pong = redis.redisCommand("PING")
println(pong)  // PONG

val result = redis.redisCommand("SET", "key", "value")
println(result)  // OK

val value = redis.redisCommand("GET", "key")
println(value)  // value

// Info command
val info = redis.redisCommand("INFO", "server")
println(info)
```

### Dynamic Port Allocation

When you don't want to specify a port:

```kotlin
RedisTemplate("my-redis") {
    dynamicPort()  // Docker assigns an available port
}.use { redis ->
    redis.startAndWait()
    
    val actualPort = redis.getMappedPort()
    println("Redis available on port: $actualPort")
    println("Connection: ${redis.connectionString()}")
}
```

### Persistence

Enable Redis persistence with AOF:

```kotlin
RedisTemplate("my-redis") {
    port(6379)
    withPersistence("redis-data")  // creates named volume
}.use { redis ->
    redis.startAndWait()
    
    // Data persists across container restarts
    redis.redisCommand("SET", "persistent-key", "value")
}

// Later, with same volume
RedisTemplate("my-redis") {
    port(6379)
    withPersistence("redis-data")  // same volume
}.use { redis ->
    redis.startAndWait()
    
    val value = redis.redisCommand("GET", "persistent-key")
    println(value)  // value
}
```

## Coming Soon

### RedisClusterTemplate

Redis Cluster with multiple nodes:

```kotlin
RedisClusterTemplate("my-cluster") {
    masters(3)
    replicasPerMaster(1)
    portBase(7000)
    password("secret")
}
```

### RedisSentinelTemplate

High-availability Redis with Sentinel:

```kotlin
RedisSentinelTemplate("my-sentinel") {
    masterName("mymaster")
    sentinels(3)
    password("secret")
    quorum(2)
}
```

### RedisStackTemplate

Redis with modules (JSON, Search, TimeSeries):

```kotlin
RedisStackTemplate("my-stack") {
    port(6379)
    insightPort(8001)  // RedisInsight UI
    modules(RedisModule.JSON, RedisModule.SEARCH, RedisModule.TIMESERIES)
}
```

## Examples

### Integration Testing

```kotlin
class RedisIntegrationTest {
    
    private lateinit var redis: RedisTemplate
    
    @BeforeEach
    fun setup() {
        redis = RedisTemplate("test-redis") {
            dynamicPort()
        }
        redis.startAndWaitBlocking()
    }
    
    @AfterEach
    fun teardown() {
        redis.close()
    }
    
    @Test
    fun `should store and retrieve values`() {
        val client = createRedisClient(redis.connectionString())
        
        client.set("key", "value")
        assertEquals("value", client.get("key"))
    }
}
```

### With Authentication

```kotlin
RedisTemplate("secure-redis") {
    port(6379)
    password("super-secret-password")
}.use { redis ->
    redis.startAndWait()
    
    // Connection string includes password
    println(redis.connectionString())
    // redis://:super-secret-password@localhost:6379
    
    // Commands automatically use password
    redis.redisCommand("SET", "key", "value")
}
```

### With Resource Limits

```kotlin
RedisTemplate("limited-redis") {
    port(6379)
    maxMemory("128mb")
    maxMemoryPolicy(MaxMemoryPolicy.ALLKEYS_LRU)
}
```

### Multiple Redis Instances

```kotlin
val cache = RedisTemplate("cache") {
    port(6379)
    maxMemory("256mb")
}

val sessions = RedisTemplate("sessions") {
    port(6380)
    password("secret")
    withPersistence("session-data")
}

cache.startAndWait()
sessions.startAndWait()

try {
    // Use both Redis instances
    println("Cache: ${cache.connectionString()}")
    println("Sessions: ${sessions.connectionString()}")
} finally {
    cache.close()
    sessions.close()
}
```
