# Quick Start

This guide walks you through the basics of using docker-kotlin to run containers.

## Running Your First Container

The simplest way to run a container:

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.core.Docker

    suspend fun main() {
        val docker = Docker()
        
        // Run nginx in the background
        val containerId = docker.run("nginx:alpine") {
            name("my-nginx")
            port(8080, 80)
            detach()
        }
        
        println("Container started: ${containerId.short()}")
        
        // Clean up
        docker.stop("my-nginx")
        docker.rm("my-nginx")
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.core.Docker;
    import io.github.joshrotenberg.dockerkotlin.core.command.*;

    public class Main {
        public static void main(String[] args) {
            Docker docker = Docker.create();
            
            // Run nginx in the background
            ContainerId containerId = RunCommand.builder("nginx:alpine")
                .name("my-nginx")
                .port(8080, 80)
                .detach()
                .executeBlocking();
            
            System.out.println("Container started: " + containerId.short());
            
            // Clean up
            StopCommand.builder("my-nginx").executeBlocking();
            RmCommand.builder("my-nginx").executeBlocking();
        }
    }
    ```

## Running with Environment Variables

Pass configuration to your container:

=== "Kotlin"

    ```kotlin
    val containerId = docker.run("postgres:16") {
        name("my-postgres")
        port(5432, 5432)
        env("POSTGRES_USER", "myuser")
        env("POSTGRES_PASSWORD", "mypassword")
        env("POSTGRES_DB", "mydb")
        detach()
    }
    ```

=== "Java"

    ```java
    ContainerId containerId = RunCommand.builder("postgres:16")
        .name("my-postgres")
        .port(5432, 5432)
        .env("POSTGRES_USER", "myuser")
        .env("POSTGRES_PASSWORD", "mypassword")
        .env("POSTGRES_DB", "mydb")
        .detach()
        .executeBlocking();
    ```

## Mounting Volumes

Persist data or share files with containers:

=== "Kotlin"

    ```kotlin
    val containerId = docker.run("nginx:alpine") {
        name("web-server")
        port(8080, 80)
        volume("/path/to/html", "/usr/share/nginx/html")
        volume("/path/to/config", "/etc/nginx/conf.d", "ro")  // read-only
        detach()
    }
    ```

=== "Java"

    ```java
    ContainerId containerId = RunCommand.builder("nginx:alpine")
        .name("web-server")
        .port(8080, 80)
        .volume("/path/to/html", "/usr/share/nginx/html")
        .volume("/path/to/config", "/etc/nginx/conf.d", "ro")
        .detach()
        .executeBlocking();
    ```

## Running Interactive Commands

Run a command and get the output:

=== "Kotlin"

    ```kotlin
    // Run a one-off command
    val output = RunCommand("alpine")
        .rm()  // auto-remove when done
        .command("echo", "Hello from Docker!")
        .execute()
    
    // output contains the container ID (command output goes to stdout)
    ```

=== "Java"

    ```java
    ContainerId output = RunCommand.builder("alpine")
        .rm()
        .command("echo", "Hello from Docker!")
        .executeBlocking();
    ```

## Using Templates

Templates provide pre-configured containers with sensible defaults:

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate

    // Use try-with-resources pattern
    RedisTemplate("my-redis") {
        port(6379)
        password("secret")
    }.use { redis ->
        redis.startAndWait()
        
        println("Redis connection: ${redis.connectionString()}")
        // redis://:secret@localhost:6379
        
        // Execute Redis commands
        val pong = redis.redisCommand("PING")
        println("Redis says: $pong")  // PONG
    }
    // Container automatically stopped and removed
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.redis.RedisTemplate;

    try (RedisTemplate redis = RedisTemplate.builder("my-redis")
            .port(6379)
            .password("secret")
            .build()) {
        
        redis.startAndWaitBlocking();
        
        System.out.println("Redis connection: " + redis.connectionString());
        
    } // Container automatically stopped and removed
    ```

## Error Handling

docker-kotlin uses a sealed exception hierarchy:

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.core.error.DockerException

    try {
        docker.run("nonexistent:image") {
            detach()
        }
    } catch (e: DockerException.CommandFailed) {
        println("Command failed: ${e.stderr}")
        println("Exit code: ${e.exitCode}")
    } catch (e: DockerException.Timeout) {
        println("Command timed out after ${e.duration}")
    } catch (e: DockerException) {
        println("Docker error: ${e.message}")
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.core.error.DockerException;

    try {
        RunCommand.builder("nonexistent:image")
            .detach()
            .executeBlocking();
    } catch (DockerException.CommandFailed e) {
        System.out.println("Command failed: " + e.getStderr());
        System.out.println("Exit code: " + e.getExitCode());
    } catch (DockerException.Timeout e) {
        System.out.println("Command timed out after " + e.getDuration());
    } catch (DockerException e) {
        System.out.println("Docker error: " + e.getMessage());
    }
    ```

## Next Steps

- [Configuration](configuration.md) - Customize Docker client behavior
- [Commands Overview](../commands/overview.md) - Full command reference
- [Templates](../container-templates/overview.md) - Pre-configured containers
- [Examples](../examples/basic.md) - More usage examples
