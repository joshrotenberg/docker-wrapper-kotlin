# Image Commands

Commands for managing Docker images.

## PullCommand

Pull an image from a registry.

=== "Kotlin"

    ```kotlin
    // Pull latest
    docker.pull("nginx")
    
    // Pull specific tag
    docker.pull("nginx:1.25-alpine")
    
    // Pull with options
    docker.pull("nginx:alpine") {
        platform("linux/amd64")  // specific platform
        quiet()                  // suppress output
    }
    
    // Pull all tags
    PullCommand("nginx")
        .allTags()
        .execute()
    ```

=== "Java"

    ```java
    PullCommand.builder("nginx:alpine")
        .platform("linux/amd64")
        .quiet()
        .executeBlocking();
    ```

## BuildCommand

Build an image from a Dockerfile.

=== "Kotlin"

    ```kotlin
    // Build from current directory
    val imageId = BuildCommand(".")
        .tag("my-app:latest")
        .execute()
    
    // Build with options
    BuildCommand("./docker")
        .tag("my-app:v1.0")
        .tag("my-app:latest")         // multiple tags
        .file("Dockerfile.prod")      // specify Dockerfile
        .buildArg("VERSION", "1.0")   // build arguments
        .target("production")         // multi-stage target
        .noCache()                    // don't use cache
        .pull()                       // always pull base images
        .quiet()                      // suppress output
        .execute()
    
    // Build with labels
    BuildCommand(".")
        .tag("my-app:latest")
        .label("version", "1.0")
        .label("maintainer", "team@example.com")
        .execute()
    ```

=== "Java"

    ```java
    String imageId = BuildCommand.builder(".")
        .tag("my-app:latest")
        .file("Dockerfile.prod")
        .buildArg("VERSION", "1.0")
        .target("production")
        .noCache()
        .executeBlocking();
    ```

### Build Context

```kotlin
// Build from directory
BuildCommand("/path/to/context")

// Build from Git repository
BuildCommand("https://github.com/user/repo.git")

// Build from tar archive
BuildCommand("archive.tar.gz")
```

## ImagesCommand

List images.

=== "Kotlin"

    ```kotlin
    val images = ImagesCommand()
        .execute()
    
    images.forEach { image ->
        println("${image.repository}:${image.tag} - ${image.size}")
    }
    
    // Filter images
    ImagesCommand()
        .filter("reference=nginx*")
        .filter("dangling=false")
        .execute()
    
    // Show all images (including intermediate)
    ImagesCommand()
        .all()
        .execute()
    ```

=== "Java"

    ```java
    List<ImageInfo> images = ImagesCommand.builder()
        .filter("reference=nginx*")
        .executeBlocking();
    ```

## TagCommand

Create a new tag for an image.

```kotlin
TagCommand("my-app:latest", "my-app:v1.0")
    .execute()

// Tag for remote registry
TagCommand("my-app:latest", "registry.example.com/my-app:v1.0")
    .execute()
```

## PushCommand

Push an image to a registry.

=== "Kotlin"

    ```kotlin
    // Push to Docker Hub
    PushCommand("username/my-app:latest")
        .execute()
    
    // Push to private registry
    PushCommand("registry.example.com/my-app:v1.0")
        .execute()
    
    // Push all tags
    PushCommand("my-app")
        .allTags()
        .execute()
    ```

=== "Java"

    ```java
    PushCommand.builder("username/my-app:latest")
        .executeBlocking();
    ```

## RmiCommand

Remove one or more images.

```kotlin
// Remove single image
RmiCommand("my-app:old")
    .execute()

// Remove multiple images
RmiCommand("image1:tag", "image2:tag", "image3:tag")
    .execute()

// Force removal
RmiCommand("my-app:latest")
    .force()     // remove even if used by containers
    .noPrune()   // don't delete untagged parents
    .execute()
```

## SaveCommand

Save images to a tar archive.

```kotlin
// Save single image
SaveCommand("my-app:latest")
    .output("/backup/my-app.tar")
    .execute()

// Save multiple images
SaveCommand("app1:latest", "app2:latest")
    .output("/backup/apps.tar")
    .execute()
```

## LoadCommand

Load images from a tar archive.

```kotlin
LoadCommand()
    .input("/backup/my-app.tar")
    .quiet()  // suppress output
    .execute()
```

## HistoryCommand

Show the history of an image.

```kotlin
val layers = HistoryCommand("my-app:latest")
    .execute()

layers.forEach { layer ->
    println("${layer.createdBy} - ${layer.size}")
}

// Show full commands (not truncated)
HistoryCommand("my-app:latest")
    .noTrunc()
    .execute()
```

## SearchCommand

Search Docker Hub for images.

```kotlin
val results = SearchCommand("nginx")
    .limit(25)
    .execute()

results.forEach { result ->
    println("${result.name}: ${result.description}")
    println("  Stars: ${result.stars}, Official: ${result.official}")
}

// Filter by stars
SearchCommand("redis")
    .filter("stars=100")
    .filter("is-official=true")
    .execute()
```

## ImportCommand

Import contents from a tarball to create an image.

```kotlin
ImportCommand("/path/to/rootfs.tar")
    .repository("my-image")
    .tag("latest")
    .message("Imported from tarball")
    .change("ENV PATH=/usr/local/bin:\$PATH")
    .execute()
```

## Common Patterns

### Build and Push

```kotlin
// Build image
BuildCommand(".")
    .tag("registry.example.com/my-app:v1.0")
    .execute()

// Push to registry
PushCommand("registry.example.com/my-app:v1.0")
    .execute()
```

### Clean Up Dangling Images

```kotlin
// Find dangling images
val dangling = ImagesCommand()
    .filter("dangling=true")
    .execute()

// Remove them
dangling.forEach { image ->
    RmiCommand(image.id).execute()
}
```

### Backup and Restore Images

```kotlin
// Backup
SaveCommand("my-app:latest", "my-db:latest")
    .output("/backup/images.tar")
    .execute()

// Restore
LoadCommand()
    .input("/backup/images.tar")
    .execute()
```

### Multi-Platform Build

```kotlin
// Build for multiple platforms (requires buildx)
BuildCommand(".")
    .tag("my-app:latest")
    .platform("linux/amd64")
    .platform("linux/arm64")
    .push()  // push to registry
    .execute()
```
