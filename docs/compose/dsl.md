# Compose DSL

The Compose DSL provides a type-safe way to define Docker Compose configurations directly in Kotlin or Java code. This is particularly powerful for integration testing, where you can define your test infrastructure alongside your test code.

## Quick Start

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.compose.dsl.*

    val compose = dockerCompose {
        service("redis") {
            image = "redis:7-alpine"
            ports("6379:6379")
        }
    }

    // Generate YAML
    println(compose.toYaml())

    // Or run directly
    compose.upBlocking { detach() }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.compose.dsl.*;

    ComposeSpec compose = ComposeBuilder.create()
        .service("redis", redis -> redis
            .image("redis:7-alpine")
            .ports("6379:6379"))
        .build();

    // Generate YAML
    System.out.println(compose.toYaml());
    ```

## Service Configuration

### Basic Service

=== "Kotlin"

    ```kotlin
    service("web") {
        image = "nginx:alpine"
        containerName = "my-nginx"
        ports("8080:80", "8443:443")
        restart = "unless-stopped"
    }
    ```

=== "Java"

    ```java
    .service("web", web -> web
        .image("nginx:alpine")
        .containerName("my-nginx")
        .ports("8080:80", "8443:443")
        .restart("unless-stopped"))
    ```

### Environment Variables

=== "Kotlin"

    ```kotlin
    service("api") {
        image = "myapp/api:latest"
        environment {
            "NODE_ENV" to "production"
            "PORT" to 3000
            "DEBUG" to false
        }
        // Or individually
        environment("DATABASE_URL", "postgres://db:5432/app")
    }
    ```

=== "Java"

    ```java
    .service("api", api -> api
        .image("myapp/api:latest")
        .environment("NODE_ENV", "production")
        .environment("PORT", "3000")
        .environment("DATABASE_URL", "postgres://db:5432/app"))
    ```

### Volumes

=== "Kotlin"

    ```kotlin
    service("db") {
        image = "postgres:16"
        // Named volume
        volume("pgdata", "/var/lib/postgresql/data")
        // Bind mount
        volume("./init", "/docker-entrypoint-initdb.d", "ro")
        // Short syntax
        volumes("./config:/etc/app/config:ro")
    }

    // Define named volume
    volume("pgdata") {
        driver = "local"
    }
    ```

=== "Java"

    ```java
    .service("db", db -> db
        .image("postgres:16")
        .volume("pgdata", "/var/lib/postgresql/data")
        .volume("./init", "/docker-entrypoint-initdb.d", "ro"))
    .volume("pgdata", vol -> vol.driver("local"))
    ```

### Networks

=== "Kotlin"

    ```kotlin
    service("api") {
        image = "myapp/api:latest"
        networks("frontend", "backend")
    }

    network("frontend") {
        driver = "bridge"
    }

    network("backend") {
        driver = "bridge"
        internal = true  // No external access
    }
    ```

=== "Java"

    ```java
    .service("api", api -> api
        .image("myapp/api:latest")
        .networks("frontend", "backend"))
    .network("frontend", net -> net.driver("bridge"))
    .network("backend", net -> net
        .driver("bridge")
        .internal(true))
    ```

### Dependencies

=== "Kotlin"

    ```kotlin
    // Simple dependencies
    service("web") {
        image = "nginx:alpine"
        dependsOn("api", "redis")
    }

    // With conditions
    service("web") {
        image = "nginx:alpine"
        dependsOn {
            serviceHealthy("api")    // Wait for healthcheck
            serviceStarted("redis")  // Just wait for start
        }
    }
    ```

=== "Java"

    ```java
    .service("web", web -> web
        .image("nginx:alpine")
        .dependsOn("api", "redis"))
    ```

### Healthcheck

=== "Kotlin"

    ```kotlin
    service("api") {
        image = "myapp/api:latest"
        healthcheck {
            testShell("curl -f http://localhost:3000/health || exit 1")
            interval = "30s"
            timeout = "10s"
            retries = 3
            startPeriod = "5s"
        }
    }

    // Or with CMD form
    service("db") {
        image = "postgres:16"
        healthcheck {
            testCmd("pg_isready", "-U", "postgres")
            interval = "10s"
            retries = 5
        }
    }
    ```

=== "Java"

    ```java
    .service("api", api -> api
        .image("myapp/api:latest")
        .healthcheck(hc -> hc
            .testShell("curl -f http://localhost:3000/health || exit 1")
            .interval("30s")
            .timeout("10s")
            .retries(3)
            .startPeriod("5s")))
    ```

### Build Configuration

=== "Kotlin"

    ```kotlin
    service("app") {
        build("./app") {
            dockerfile = "Dockerfile.prod"
            target = "production"
            arg("VERSION", "1.0.0")
            arg("BUILD_DATE", System.getenv("BUILD_DATE") ?: "unknown")
            cacheFrom("myapp:latest")
        }
    }
    ```

=== "Java"

    ```java
    .service("app", app -> app
        .build("./app", build -> build
            .dockerfile("Dockerfile.prod")
            .target("production")
            .arg("VERSION", "1.0.0")))
    ```

### Deploy (Swarm Mode)

=== "Kotlin"

    ```kotlin
    service("api") {
        image = "myapp/api:latest"
        deploy {
            replicas = 3
            resources {
                limits {
                    cpus = "0.5"
                    memory = "512M"
                }
                reservations {
                    cpus = "0.25"
                    memory = "256M"
                }
            }
            restartPolicy {
                condition = "on-failure"
                maxAttempts = 3
                delay = "5s"
            }
        }
    }
    ```

=== "Java"

    ```java
    .service("api", api -> api
        .image("myapp/api:latest")
        .deploy(deploy -> deploy
            .replicas(3)
            .resources(res -> res
                .cpus("0.5")
                .memory("512M"))))
    ```

### Logging

=== "Kotlin"

    ```kotlin
    service("app") {
        image = "myapp:latest"
        logging("json-file") {
            maxSize("50m")
            maxFile(5)
            option("compress", "true")
        }
    }
    ```

=== "Java"

    ```java
    .service("app", app -> app
        .image("myapp:latest")
        .logging("json-file", log -> log
            .maxSize("50m")
            .maxFile(5)))
    ```

## Running Compose Stacks

### Generate YAML Only

```kotlin
val compose = dockerCompose {
    service("redis") { image = "redis:7" }
}

// Get YAML string
val yaml = compose.toYaml()

// Write to file
compose.writeTo("docker-compose.yml")
compose.writeTo(File("docker-compose.yml"))
```

### Run with Commands

```kotlin
val compose = dockerCompose {
    service("redis") { image = "redis:7" }
}

// Start the stack
compose.upBlocking {
    detach()
    wait()  // Wait for healthchecks
}

// Stop the stack
compose.downBlocking {
    volumes()  // Also remove volumes
}
```

### Automatic Cleanup with use()

```kotlin
dockerCompose {
    service("redis") { image = "redis:7-alpine" }
}.use("my-test-stack") {
    // Stack is running here
    // Do your testing...
}
// Stack is automatically stopped and cleaned up
```

## Testing Examples

### JUnit 5 Integration Test

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.compose.dsl.*
    import org.junit.jupiter.api.*

    class UserServiceIntegrationTest {

        companion object {
            private val testStack = dockerCompose {
                service("postgres") {
                    image = "postgres:16-alpine"
                    environment {
                        "POSTGRES_USER" to "test"
                        "POSTGRES_PASSWORD" to "test"
                        "POSTGRES_DB" to "testdb"
                    }
                    ports("5432:5432")
                    healthcheck {
                        testCmd("pg_isready", "-U", "test")
                        interval = "5s"
                        retries = 5
                    }
                }

                service("redis") {
                    image = "redis:7-alpine"
                    ports("6379:6379")
                    healthcheck {
                        testCmd("redis-cli", "ping")
                        interval = "5s"
                        retries = 3
                    }
                }
            }

            @BeforeAll
            @JvmStatic
            fun startContainers() {
                testStack.upBlocking {
                    detach()
                    wait()  // Wait for healthchecks to pass
                }
            }

            @AfterAll
            @JvmStatic
            fun stopContainers() {
                testStack.downBlocking {
                    volumes()
                }
            }
        }

        @Test
        fun `user can be created and retrieved`() {
            // Both postgres and redis are ready
            val userService = UserService(
                dbUrl = "jdbc:postgresql://localhost:5432/testdb",
                redisUrl = "redis://localhost:6379"
            )

            val user = userService.createUser("test@example.com")
            assertNotNull(user.id)
        }

        @Test
        fun `user is cached in redis`() {
            // Test caching behavior
        }
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.compose.dsl.*;
    import org.junit.jupiter.api.*;

    class UserServiceIntegrationTest {

        private static ComposeSpec testStack;

        @BeforeAll
        static void startContainers() {
            testStack = ComposeBuilder.create()
                .service("postgres", pg -> pg
                    .image("postgres:16-alpine")
                    .environment("POSTGRES_USER", "test")
                    .environment("POSTGRES_PASSWORD", "test")
                    .environment("POSTGRES_DB", "testdb")
                    .ports("5432:5432")
                    .healthcheck(hc -> hc
                        .testCmd("pg_isready", "-U", "test")
                        .interval("5s")
                        .retries(5)))
                .service("redis", redis -> redis
                    .image("redis:7-alpine")
                    .ports("6379:6379")
                    .healthcheck(hc -> hc
                        .testCmd("redis-cli", "ping")
                        .interval("5s")
                        .retries(3)))
                .build();

            testStack.runner()
                .projectName("user-service-test")
                .up()
                .detach()
                .wait()
                .executeBlocking();
        }

        @AfterAll
        static void stopContainers() {
            testStack.runner()
                .projectName("user-service-test")
                .down()
                .volumes()
                .executeBlocking();
        }

        @Test
        void userCanBeCreatedAndRetrieved() {
            // Test implementation
        }
    }
    ```

### Per-Test Container Lifecycle

```kotlin
class IsolatedTest {

    @Test
    fun `each test gets fresh containers`() {
        dockerCompose {
            service("redis") {
                image = "redis:7-alpine"
                ports("6379:6379")
            }
        }.use("isolated-${UUID.randomUUID()}") {
            // Fresh Redis for this test only
            val redis = Jedis("localhost", 6379)
            redis.set("key", "value")
            assertEquals("value", redis.get("key"))
        }
        // Container cleaned up automatically
    }
}
```

### Reusable Test Fixtures

```kotlin
object TestContainers {
    val postgres = dockerCompose {
        service("postgres") {
            image = "postgres:16-alpine"
            environment {
                "POSTGRES_USER" to "test"
                "POSTGRES_PASSWORD" to "test"
                "POSTGRES_DB" to "testdb"
            }
            ports("5432:5432")
            healthcheck {
                testCmd("pg_isready", "-U", "test")
                interval = "5s"
                retries = 5
            }
        }
    }

    val redis = dockerCompose {
        service("redis") {
            image = "redis:7-alpine"
            ports("6379:6379")
        }
    }

    val fullStack = dockerCompose {
        service("postgres") {
            image = "postgres:16-alpine"
            environment("POSTGRES_PASSWORD", "test")
            ports("5432:5432")
        }
        service("redis") {
            image = "redis:7-alpine"
            ports("6379:6379")
        }
        service("rabbitmq") {
            image = "rabbitmq:3-management-alpine"
            ports("5672:5672", "15672:15672")
        }
    }
}

// Usage in tests
class DatabaseTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            TestContainers.postgres.upBlocking { detach(); wait() }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            TestContainers.postgres.downBlocking { volumes() }
        }
    }
}
```

### Dynamic Configuration

```kotlin
fun createTestStack(
    postgresVersion: String = "16",
    redisVersion: String = "7",
    enableMonitoring: Boolean = false
) = dockerCompose {
    service("postgres") {
        image = "postgres:$postgresVersion-alpine"
        environment("POSTGRES_PASSWORD", "test")
        ports("5432:5432")
    }

    service("redis") {
        image = "redis:$redisVersion-alpine"
        ports("6379:6379")
    }

    if (enableMonitoring) {
        service("prometheus") {
            image = "prom/prometheus:latest"
            ports("9090:9090")
            volumes("./prometheus.yml:/etc/prometheus/prometheus.yml:ro")
        }

        service("grafana") {
            image = "grafana/grafana:latest"
            ports("3000:3000")
            dependsOn("prometheus")
        }
    }
}

// Usage
val stack = createTestStack(
    postgresVersion = "15",
    enableMonitoring = true
)
```

## Full Example

```kotlin
val productionStack = dockerCompose {
    name = "myapp-production"

    // Frontend
    service("nginx") {
        image = "nginx:alpine"
        ports("80:80", "443:443")
        volumes(
            "./nginx/nginx.conf:/etc/nginx/nginx.conf:ro",
            "./nginx/certs:/etc/nginx/certs:ro"
        )
        networks("frontend")
        dependsOn {
            serviceHealthy("api")
        }
        restart = "unless-stopped"
        logging("json-file") {
            maxSize("50m")
            maxFile(5)
        }
    }

    // API
    service("api") {
        image = "myapp/api:latest"
        environment {
            "NODE_ENV" to "production"
            "DATABASE_URL" to "postgres://postgres:5432/app"
            "REDIS_URL" to "redis://redis:6379"
            "JWT_SECRET" to System.getenv("JWT_SECRET")
        }
        networks("frontend", "backend")
        dependsOn {
            serviceHealthy("postgres")
            serviceHealthy("redis")
        }
        healthcheck {
            testShell("curl -f http://localhost:3000/health || exit 1")
            interval = "30s"
            timeout = "10s"
            retries = 3
            startPeriod = "10s"
        }
        deploy {
            replicas = 2
            resources {
                limits {
                    cpus = "1"
                    memory = "1G"
                }
            }
            restartPolicy {
                condition = "on-failure"
                maxAttempts = 3
            }
        }
    }

    // Database
    service("postgres") {
        image = "postgres:16-alpine"
        environment {
            "POSTGRES_USER" to "app"
            "POSTGRES_PASSWORD" to System.getenv("DB_PASSWORD")
            "POSTGRES_DB" to "app"
        }
        volumes("pgdata:/var/lib/postgresql/data")
        networks("backend")
        healthcheck {
            testCmd("pg_isready", "-U", "app")
            interval = "10s"
            retries = 5
        }
    }

    // Cache
    service("redis") {
        image = "redis:7-alpine"
        command("redis-server", "--appendonly", "yes")
        volumes("redis-data:/data")
        networks("backend")
        healthcheck {
            testCmd("redis-cli", "ping")
            interval = "10s"
            retries = 3
        }
    }

    // Networks
    network("frontend") {
        driver = "bridge"
    }

    network("backend") {
        driver = "bridge"
        internal = true
    }

    // Volumes
    volume("pgdata") {
        driver = "local"
    }

    volume("redis-data") {
        driver = "local"
    }
}

// Generate docker-compose.yml
productionStack.writeTo("docker-compose.prod.yml")
```
