# Development Examples

Using docker-kotlin for local development workflows.

## Development Environment Script

```kotlin
#!/usr/bin/env kotlin

@file:DependsOn("io.github.joshrotenberg:docker-kotlin-compose:0.1.0")

import io.github.joshrotenberg.dockerkotlin.compose.*

suspend fun main(args: Array<String>) {
    val command = args.firstOrNull() ?: "up"
    
    when (command) {
        "up" -> startDevEnv()
        "down" -> stopDevEnv()
        "logs" -> showLogs()
        "status" -> showStatus()
        else -> println("Usage: dev.kt [up|down|logs|status]")
    }
}

suspend fun startDevEnv() {
    println("Starting development environment...")
    
    ComposeUpCommand()
        .file("docker-compose.yml")
        .file("docker-compose.dev.yml")
        .projectName("myapp-dev")
        .build()
        .detach()
        .wait()
        .execute()
    
    println("Development environment started!")
    showStatus()
}

suspend fun stopDevEnv() {
    println("Stopping development environment...")
    
    ComposeDownCommand()
        .file("docker-compose.yml")
        .file("docker-compose.dev.yml")
        .projectName("myapp-dev")
        .volumes()
        .execute()
    
    println("Development environment stopped.")
}

suspend fun showLogs() {
    ComposeLogsCommand()
        .file("docker-compose.yml")
        .projectName("myapp-dev")
        .follow()
        .execute()
}

suspend fun showStatus() {
    val services = ComposePsCommand()
        .file("docker-compose.yml")
        .projectName("myapp-dev")
        .execute()
    
    println("\nServices:")
    services.forEach { service ->
        println("  ${service.name}: ${service.status}")
    }
}
```

## Database Migration Runner

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun runMigrations(
    migrationsDir: String,
    databaseUrl: String
) {
    val docker = Docker()
    
    // Run flyway migrations
    val containerId = docker.run("flyway/flyway:latest") {
        rm()  // auto-remove
        volume(migrationsDir, "/flyway/sql")
        command(
            "-url=$databaseUrl",
            "-user=postgres",
            "-password=secret",
            "migrate"
        )
    }
    
    println("Migrations completed")
}

// Usage
suspend fun main() {
    runMigrations(
        migrationsDir = "./db/migrations",
        databaseUrl = "jdbc:postgresql://localhost:5432/mydb"
    )
}
```

## Local Development Stack

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import io.github.joshrotenberg.dockerkotlin.core.command.*
import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate
import kotlinx.coroutines.*

class DevStack : AutoCloseable {
    private val docker = Docker()
    private val containers = mutableListOf<String>()
    
    private lateinit var redis: RedisTemplate
    private lateinit var postgresId: ContainerId
    private lateinit var mailhogId: ContainerId
    
    suspend fun start() {
        println("Starting development stack...")
        
        // Create network
        NetworkCreateCommand("dev-network").execute()
        
        coroutineScope {
            // Start services in parallel
            val redisJob = async { startRedis() }
            val postgresJob = async { startPostgres() }
            val mailhogJob = async { startMailhog() }
            
            redisJob.await()
            postgresJob.await()
            mailhogJob.await()
        }
        
        printInfo()
    }
    
    private suspend fun startRedis() {
        redis = RedisTemplate("dev-redis") {
            network = "dev-network"
            networkAliases.add("redis")
            port(6379)
        }
        redis.startAndWait()
        containers.add("dev-redis")
        println("  Redis started")
    }
    
    private suspend fun startPostgres() {
        postgresId = docker.run("postgres:16") {
            name("dev-postgres")
            network("dev-network")
            networkAlias("database")
            port(5432, 5432)
            env("POSTGRES_USER", "dev")
            env("POSTGRES_PASSWORD", "dev")
            env("POSTGRES_DB", "devdb")
            namedVolume("dev-postgres-data", "/var/lib/postgresql/data")
            detach()
        }
        containers.add("dev-postgres")
        
        // Wait for ready
        waitForPostgres()
        println("  PostgreSQL started")
    }
    
    private suspend fun waitForPostgres() {
        repeat(30) {
            try {
                val result = ExecCommand(postgresId.value, "pg_isready", "-U", "dev")
                    .execute()
                if (result.exitCode == 0) return
            } catch (e: Exception) {}
            delay(1000)
        }
    }
    
    private suspend fun startMailhog() {
        mailhogId = docker.run("mailhog/mailhog:latest") {
            name("dev-mailhog")
            network("dev-network")
            networkAlias("mail")
            port(1025, 1025)  // SMTP
            port(8025, 8025)  // Web UI
            detach()
        }
        containers.add("dev-mailhog")
        println("  MailHog started")
    }
    
    private fun printInfo() {
        println("\n=== Development Stack Ready ===")
        println()
        println("Services:")
        println("  Redis:      redis://localhost:6379")
        println("  PostgreSQL: postgresql://dev:dev@localhost:5432/devdb")
        println("  MailHog:    http://localhost:8025 (SMTP: localhost:1025)")
        println()
        println("Network: dev-network")
        println()
    }
    
    override fun close() {
        println("Stopping development stack...")
        runBlocking {
            containers.reversed().forEach { name ->
                try {
                    docker.stop(name)
                    docker.rm(name)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            try {
                NetworkRmCommand("dev-network").execute()
            } catch (e: Exception) {
                // Ignore
            }
        }
        println("Development stack stopped.")
    }
}

suspend fun main() {
    val stack = DevStack()
    stack.start()
    
    println("Press Enter to stop...")
    readLine()
    
    stack.close()
}
```

## Hot Reload Development

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import io.github.joshrotenberg.dockerkotlin.core.command.*
import java.nio.file.*

suspend fun main() {
    val docker = Docker()
    val projectDir = Paths.get(".").toAbsolutePath().toString()
    
    // Start with mounted source code
    val containerId = docker.run("node:20-alpine") {
        name("dev-app")
        port(3000, 3000)
        volume(projectDir, "/app")
        workdir("/app")
        env("NODE_ENV", "development")
        command("npm", "run", "dev")
        detach()
    }
    
    println("Development server started at http://localhost:3000")
    println("Source code mounted from: $projectDir")
    
    // Follow logs
    LogsCommand(containerId.value)
        .follow()
        .execute()
}
```

## CI Pipeline Script

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*
import io.github.joshrotenberg.dockerkotlin.compose.*
import kotlin.system.exitProcess

suspend fun main() {
    try {
        // Build
        println("=== Building ===")
        BuildCommand(".")
            .tag("myapp:${System.getenv("CI_COMMIT_SHA") ?: "local"}")
            .tag("myapp:latest")
            .noCache()
            .execute()
        
        // Start test dependencies
        println("\n=== Starting Test Environment ===")
        ComposeUpCommand()
            .file("docker-compose.test.yml")
            .detach()
            .wait()
            .execute()
        
        // Run tests
        println("\n=== Running Tests ===")
        val testResult = ComposeRunCommand("app", "npm", "test")
            .file("docker-compose.test.yml")
            .rm()
            .execute()
        
        // Cleanup
        println("\n=== Cleanup ===")
        ComposeDownCommand()
            .file("docker-compose.test.yml")
            .volumes()
            .execute()
        
        if (testResult.exitCode != 0) {
            println("Tests failed!")
            exitProcess(1)
        }
        
        // Push if on main branch
        if (System.getenv("CI_BRANCH") == "main") {
            println("\n=== Pushing Image ===")
            val registry = System.getenv("CI_REGISTRY") ?: "registry.example.com"
            
            TagCommand("myapp:latest", "$registry/myapp:latest").execute()
            PushCommand("$registry/myapp:latest").execute()
        }
        
        println("\n=== CI Pipeline Complete ===")
        
    } catch (e: Exception) {
        println("Pipeline failed: ${e.message}")
        exitProcess(1)
    }
}
```

## Database Seeding

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun seedDatabase(containerName: String, seedFile: String) {
    // Copy seed file to container
    CpCommand(seedFile, "$containerName:/tmp/seed.sql")
        .execute()
    
    // Execute seed file
    val result = ExecCommand(
        containerName,
        "psql", "-U", "postgres", "-d", "mydb", "-f", "/tmp/seed.sql"
    ).execute()
    
    if (result.exitCode == 0) {
        println("Database seeded successfully")
    } else {
        println("Seeding failed: ${result.stderr}")
    }
}

suspend fun main() {
    seedDatabase("dev-postgres", "./db/seeds/development.sql")
}
```

## Interactive Shell

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun shell(containerName: String) {
    // Start interactive shell
    ExecCommand(containerName, "/bin/bash")
        .interactive()
        .tty()
        .execute()
}

// For containers without bash
suspend fun shellAlpine(containerName: String) {
    ExecCommand(containerName, "/bin/sh")
        .interactive()
        .tty()
        .execute()
}
```
