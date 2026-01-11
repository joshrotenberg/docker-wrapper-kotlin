# Basic Examples

Common usage patterns for docker-kotlin.

## Running a Web Server

=== "Kotlin"

    ```kotlin
    import io.github.joshrotenberg.dockerkotlin.core.Docker

    suspend fun main() {
        val docker = Docker()
        
        // Run nginx
        val containerId = docker.run("nginx:alpine") {
            name("web-server")
            port(8080, 80)
            detach()
        }
        
        println("Nginx running at http://localhost:8080")
        println("Container ID: ${containerId.short()}")
        
        // Press enter to stop
        readLine()
        
        docker.stop("web-server")
        docker.rm("web-server")
    }
    ```

=== "Java"

    ```java
    import io.github.joshrotenberg.dockerkotlin.core.Docker;
    import io.github.joshrotenberg.dockerkotlin.core.command.*;

    public class WebServer {
        public static void main(String[] args) {
            Docker docker = Docker.create();
            
            ContainerId containerId = RunCommand.builder("nginx:alpine")
                .name("web-server")
                .port(8080, 80)
                .detach()
                .executeBlocking();
            
            System.out.println("Nginx running at http://localhost:8080");
            System.out.println("Container ID: " + containerId.short());
            
            // Cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                StopCommand.builder("web-server").executeBlocking();
                RmCommand.builder("web-server").executeBlocking();
            }));
        }
    }
    ```

## Running a Database

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val docker = Docker()
    
    // Run PostgreSQL
    val containerId = docker.run("postgres:16") {
        name("my-postgres")
        port(5432, 5432)
        env("POSTGRES_USER", "myuser")
        env("POSTGRES_PASSWORD", "mypassword")
        env("POSTGRES_DB", "mydb")
        namedVolume("postgres-data", "/var/lib/postgresql/data")
        detach()
    }
    
    println("PostgreSQL started: ${containerId.short()}")
    println("Connection: postgresql://myuser:mypassword@localhost:5432/mydb")
    
    // Wait for database to be ready
    var ready = false
    repeat(30) {
        try {
            val result = ExecCommand(containerId.value, "pg_isready", "-U", "myuser")
                .executeBlocking()
            if (result.exitCode == 0) {
                ready = true
                return@repeat
            }
        } catch (e: Exception) {
            // Not ready yet
        }
        Thread.sleep(1000)
    }
    
    if (ready) {
        println("Database is ready!")
    } else {
        println("Database failed to start")
    }
}
```

## Building and Running Custom Image

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun main() {
    // Build image from Dockerfile
    val imageId = BuildCommand("./app")
        .tag("my-app:latest")
        .tag("my-app:v1.0")
        .buildArg("VERSION", "1.0.0")
        .execute()
    
    println("Built image: $imageId")
    
    // Run the image
    val containerId = RunCommand("my-app:latest")
        .name("my-app")
        .port(3000, 3000)
        .env("NODE_ENV", "production")
        .detach()
        .execute()
    
    println("App running at http://localhost:3000")
}
```

## Running Multiple Containers

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import io.github.joshrotenberg.dockerkotlin.core.command.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
    val docker = Docker()
    
    // Create a network
    NetworkCreateCommand("app-network").execute()
    
    // Start database
    val dbJob = async {
        docker.run("postgres:16") {
            name("db")
            network("app-network")
            networkAlias("database")
            env("POSTGRES_PASSWORD", "secret")
            detach()
        }
    }
    
    // Start cache
    val cacheJob = async {
        docker.run("redis:7-alpine") {
            name("cache")
            network("app-network")
            networkAlias("redis")
            detach()
        }
    }
    
    // Wait for infrastructure
    dbJob.await()
    cacheJob.await()
    
    // Start application
    docker.run("my-app:latest") {
        name("app")
        network("app-network")
        port(8080, 8080)
        env("DATABASE_URL", "postgresql://postgres:secret@database:5432/postgres")
        env("REDIS_URL", "redis://redis:6379")
        detach()
    }
    
    println("Application stack running!")
    println("App: http://localhost:8080")
}
```

## Executing Commands in Containers

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun main() {
    // Start a container
    val containerId = RunCommand("ubuntu:22.04")
        .name("ubuntu-test")
        .detach()
        .command("sleep", "infinity")
        .execute()
    
    // Execute commands
    val whoami = ExecCommand(containerId.value, "whoami").execute()
    println("User: ${whoami.stdout.trim()}")
    
    val hostname = ExecCommand(containerId.value, "hostname").execute()
    println("Hostname: ${hostname.stdout.trim()}")
    
    // Install a package
    ExecCommand(containerId.value, "apt-get", "update").execute()
    ExecCommand(containerId.value, "apt-get", "install", "-y", "curl").execute()
    
    // Test curl
    val curl = ExecCommand(containerId.value, "curl", "-s", "https://httpbin.org/ip").execute()
    println("External IP: ${curl.stdout}")
    
    // Cleanup
    StopCommand(containerId.value).execute()
    RmCommand(containerId.value).execute()
}
```

## Copying Files

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*
import java.io.File

suspend fun main() {
    // Start container
    val containerId = RunCommand("nginx:alpine")
        .name("nginx-test")
        .port(8080, 80)
        .detach()
        .execute()
    
    // Create a custom index.html
    File("/tmp/index.html").writeText("""
        <!DOCTYPE html>
        <html>
        <body>
            <h1>Hello from docker-kotlin!</h1>
        </body>
        </html>
    """.trimIndent())
    
    // Copy to container
    CpCommand("/tmp/index.html", "nginx-test:/usr/share/nginx/html/index.html")
        .execute()
    
    println("Custom page available at http://localhost:8080")
    
    // Copy nginx config out
    CpCommand("nginx-test:/etc/nginx/nginx.conf", "/tmp/nginx.conf")
        .execute()
    
    println("Nginx config saved to /tmp/nginx.conf")
}
```

## Viewing Logs

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

suspend fun main() {
    // Start container
    RunCommand("nginx:alpine")
        .name("nginx-logs")
        .port(8080, 80)
        .detach()
        .execute()
    
    // Generate some traffic
    repeat(5) {
        Runtime.getRuntime().exec("curl -s http://localhost:8080").waitFor()
    }
    
    // Get logs
    val logs = LogsCommand("nginx-logs")
        .tail(20)
        .timestamps()
        .execute()
    
    println("Container logs:")
    println(logs.stdout)
    
    // Cleanup
    StopCommand("nginx-logs").execute()
    RmCommand("nginx-logs").execute()
}
```

## Error Handling

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.Docker
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException

suspend fun main() {
    val docker = Docker()
    
    try {
        // Try to run non-existent image
        docker.run("nonexistent-image:latest") {
            detach()
        }
    } catch (e: DockerException.CommandFailed) {
        println("Command failed!")
        println("Exit code: ${e.exitCode}")
        println("Error: ${e.stderr}")
    } catch (e: DockerException.ImageNotFound) {
        println("Image not found: ${e.image}")
    } catch (e: DockerException.Timeout) {
        println("Command timed out after ${e.duration}")
    } catch (e: DockerException) {
        println("Docker error: ${e.message}")
    }
}
```

## Dry Run / Preview

```kotlin
import io.github.joshrotenberg.dockerkotlin.core.command.*

fun main() {
    val cmd = RunCommand("nginx:alpine")
        .name("web-server")
        .port(8080, 80)
        .port(8443, 443)
        .env("NGINX_HOST", "localhost")
        .env("NGINX_PORT", "80")
        .volume("/var/www", "/usr/share/nginx/html")
        .label("app", "nginx")
        .network("frontend")
        .memory("512m")
        .cpus("0.5")
        .restart(RestartPolicy.Always)
        .detach()
    
    // Preview without executing
    val preview = cmd.preview()
    
    println("Command that would be executed:")
    println(preview.commandLine)
    println()
    println("Arguments:")
    preview.args.forEach { println("  $it") }
}
```

Output:
```
Command that would be executed:
docker run --name web-server --publish 8080:80 --publish 8443:443 --env NGINX_HOST=localhost --env NGINX_PORT=80 --volume /var/www:/usr/share/nginx/html --label app=nginx --network frontend --memory 512m --cpus 0.5 --restart always --detach nginx:alpine

Arguments:
  run
  --name
  web-server
  --publish
  8080:80
  ...
```
