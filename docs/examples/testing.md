# Testing Examples

Using docker-kotlin for integration testing.

## JUnit 5 Integration

### Basic Test Setup

```kotlin
import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class RedisIntegrationTest {
    
    companion object {
        private lateinit var redis: RedisTemplate
        
        @JvmStatic
        @BeforeAll
        fun setupClass() {
            redis = RedisTemplate("test-redis") {
                dynamicPort()
            }
            runBlocking {
                redis.startAndWait()
            }
        }
        
        @JvmStatic
        @AfterAll
        fun teardownClass() {
            redis.close()
        }
    }
    
    @Test
    fun `should connect to redis`() = runBlocking {
        val pong = redis.redisCommand("PING")
        assertEquals("PONG", pong)
    }
    
    @Test
    fun `should store and retrieve values`() = runBlocking {
        redis.redisCommand("SET", "test-key", "test-value")
        val value = redis.redisCommand("GET", "test-key")
        assertEquals("test-value", value)
    }
}
```

### Using Extension

```kotlin
import org.junit.jupiter.api.extension.*

class DockerExtension : BeforeAllCallback, AfterAllCallback {
    
    companion object {
        private val containers = mutableMapOf<String, RedisTemplate>()
    }
    
    override fun beforeAll(context: ExtensionContext) {
        val redis = RedisTemplate("test-${context.displayName}") {
            dynamicPort()
        }
        runBlocking { redis.startAndWait() }
        containers[context.uniqueId] = redis
    }
    
    override fun afterAll(context: ExtensionContext) {
        containers.remove(context.uniqueId)?.close()
    }
    
    fun getRedis(context: ExtensionContext): RedisTemplate {
        return containers[context.uniqueId]!!
    }
}

@ExtendWith(DockerExtension::class)
class MyTest {
    @Test
    fun test(context: ExtensionContext) {
        val redis = DockerExtension().getRedis(context)
        // Use redis...
    }
}
```

## Per-Test Container

```kotlin
class IsolatedContainerTest {
    
    private lateinit var redis: RedisTemplate
    
    @BeforeEach
    fun setup() {
        redis = RedisTemplate("test-redis-${System.currentTimeMillis()}") {
            dynamicPort()
        }
        runBlocking { redis.startAndWait() }
    }
    
    @AfterEach
    fun teardown() {
        redis.close()
    }
    
    @Test
    fun `test with isolated container`() = runBlocking {
        // Each test gets a fresh container
        redis.redisCommand("SET", "key", "value")
    }
}
```

## Testing with Multiple Services

```kotlin
class FullStackTest {
    
    companion object {
        private lateinit var redis: RedisTemplate
        private lateinit var postgres: ContainerId
        private val docker = Docker()
        
        @JvmStatic
        @BeforeAll
        fun setup() = runBlocking {
            // Create network
            NetworkCreateCommand("test-network").execute()
            
            // Start Redis
            redis = RedisTemplate("test-redis") {
                network = "test-network"
                networkAliases.add("redis")
                dynamicPort()
            }
            redis.startAndWait()
            
            // Start PostgreSQL
            postgres = docker.run("postgres:16") {
                name("test-postgres")
                network("test-network")
                networkAlias("database")
                dynamicPort(5432)
                env("POSTGRES_PASSWORD", "test")
                env("POSTGRES_DB", "testdb")
                detach()
            }
            
            // Wait for Postgres
            waitForPostgres()
        }
        
        private suspend fun waitForPostgres() {
            repeat(30) {
                try {
                    val result = ExecCommand(postgres.value, "pg_isready").execute()
                    if (result.exitCode == 0) return
                } catch (e: Exception) {}
                delay(1000)
            }
            throw RuntimeException("PostgreSQL failed to start")
        }
        
        @JvmStatic
        @AfterAll
        fun teardown() = runBlocking {
            redis.close()
            docker.stop(postgres.value)
            docker.rm(postgres.value)
            NetworkRmCommand("test-network").execute()
        }
    }
    
    @Test
    fun `application connects to both services`() = runBlocking {
        // Your integration test here
        val redisUrl = redis.connectionString()
        val postgresUrl = "postgresql://postgres:test@localhost:${getMappedPort(5432)}/testdb"
        
        // Test application with these URLs
    }
}
```

## Conditional Test Execution

```kotlin
import org.junit.jupiter.api.condition.EnabledIf

class ConditionalDockerTest {
    
    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return try {
                ProcessBuilder("docker", "version")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor() == 0
            } catch (e: Exception) {
                false
            }
        }
    }
    
    @Test
    @EnabledIf("isDockerAvailable")
    fun `test requires docker`() = runBlocking {
        RedisTemplate("test-redis") {
            dynamicPort()
        }.use { redis ->
            redis.startAndWait()
            assertEquals("PONG", redis.redisCommand("PING"))
        }
    }
}
```

## Parameterized Container Tests

```kotlin
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VersionCompatibilityTest {
    
    @ParameterizedTest
    @ValueSource(strings = ["6-alpine", "7-alpine", "7.2-alpine"])
    fun `should work with Redis version`(version: String) = runBlocking {
        RedisTemplate("test-redis-$version") {
            this.version(version)
            dynamicPort()
        }.use { redis ->
            redis.startAndWait()
            
            val pong = redis.redisCommand("PING")
            assertEquals("PONG", pong)
            
            val info = redis.redisCommand("INFO", "server")
            assertTrue(info.contains("redis_version"))
        }
    }
}
```

## Testing with Test Containers Pattern

```kotlin
abstract class ContainerTest<T : Template> {
    
    protected lateinit var container: T
    
    protected abstract fun createContainer(): T
    
    @BeforeEach
    fun setup() {
        container = createContainer()
        runBlocking { container.startAndWait() }
    }
    
    @AfterEach
    fun teardown() {
        container.close()
    }
}

class RedisTest : ContainerTest<RedisTemplate>() {
    
    override fun createContainer() = RedisTemplate("test-redis") {
        dynamicPort()
    }
    
    @Test
    fun `should ping redis`() = runBlocking {
        val pong = (container as RedisTemplate).redisCommand("PING")
        assertEquals("PONG", pong)
    }
}
```

## Parallel Test Execution

```kotlin
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class ParallelContainerTest {
    
    @Test
    fun `test 1`() = runBlocking {
        // Uses unique container name
        RedisTemplate("test-redis-${Thread.currentThread().id}") {
            dynamicPort()
        }.use { redis ->
            redis.startAndWait()
            redis.redisCommand("SET", "key1", "value1")
        }
    }
    
    @Test
    fun `test 2`() = runBlocking {
        RedisTemplate("test-redis-${Thread.currentThread().id}") {
            dynamicPort()
        }.use { redis ->
            redis.startAndWait()
            redis.redisCommand("SET", "key2", "value2")
        }
    }
}
```

## Spring Boot Integration Test

```kotlin
@SpringBootTest
@Testcontainers
class SpringIntegrationTest {
    
    companion object {
        private val redis = RedisTemplate("spring-test-redis") {
            dynamicPort()
        }
        
        @JvmStatic
        @BeforeAll
        fun setup() = runBlocking {
            redis.startAndWait()
        }
        
        @JvmStatic
        @AfterAll
        fun teardown() {
            redis.close()
        }
        
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.redis.host") { "localhost" }
            registry.add("spring.redis.port") { redis.getMappedPort() }
        }
    }
    
    @Autowired
    lateinit var redisTemplate: StringRedisTemplate
    
    @Test
    fun `should use redis`() {
        redisTemplate.opsForValue().set("key", "value")
        assertEquals("value", redisTemplate.opsForValue().get("key"))
    }
}
```
