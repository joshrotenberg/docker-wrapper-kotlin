# Volume Commands

Commands for managing Docker volumes.

## VolumeCreateCommand

Create a new volume.

=== "Kotlin"

    ```kotlin
    // Create with defaults
    val volumeName = VolumeCreateCommand()
        .name("my-volume")
        .execute()
    
    // Create with options
    VolumeCreateCommand()
        .name("my-volume")
        .driver("local")
        .label("environment", "production")
        .label("app", "database")
        .execute()
    
    // Create with driver options
    VolumeCreateCommand()
        .name("nfs-volume")
        .driver("local")
        .opt("type", "nfs")
        .opt("o", "addr=192.168.1.100,rw")
        .opt("device", ":/path/to/share")
        .execute()
    ```

=== "Java"

    ```java
    String volumeName = VolumeCreateCommand.builder()
        .name("my-volume")
        .driver("local")
        .label("environment", "production")
        .executeBlocking();
    ```

## VolumeLsCommand

List volumes.

```kotlin
val volumes = VolumeLsCommand()
    .execute()

volumes.forEach { volume ->
    println("${volume.name}: ${volume.driver}")
}

// Filter volumes
VolumeLsCommand()
    .filter("driver=local")
    .filter("label=environment=production")
    .filter("dangling=true")  // unused volumes
    .execute()
```

## VolumeInspectCommand

Get detailed volume information.

```kotlin
val volume = VolumeInspectCommand("my-volume")
    .execute()

println("Name: ${volume.name}")
println("Driver: ${volume.driver}")
println("Mountpoint: ${volume.mountpoint}")
println("Created: ${volume.createdAt}")
println("Labels: ${volume.labels}")
```

## VolumeRmCommand

Remove one or more volumes.

```kotlin
// Remove single volume
VolumeRmCommand("my-volume")
    .execute()

// Remove multiple volumes
VolumeRmCommand("volume1", "volume2", "volume3")
    .execute()

// Force removal
VolumeRmCommand("my-volume")
    .force()
    .execute()
```

## VolumePruneCommand

Remove all unused volumes.

```kotlin
val result = VolumePruneCommand()
    .force()  // don't prompt
    .execute()

println("Removed volumes: ${result.volumes}")
println("Space reclaimed: ${result.spaceReclaimed}")

// Prune with filter
VolumePruneCommand()
    .filter("label!=keep")  // keep volumes with 'keep' label
    .force()
    .execute()
```

## Using Volumes with Containers

### Named Volumes

```kotlin
// Create volume
VolumeCreateCommand()
    .name("postgres-data")
    .execute()

// Use in container
docker.run("postgres:16") {
    name("db")
    namedVolume("postgres-data", "/var/lib/postgresql/data")
    detach()
}
```

### Anonymous Volumes

```kotlin
// Docker creates an anonymous volume
docker.run("postgres:16") {
    name("db")
    volume("/var/lib/postgresql/data")  // anonymous volume
    detach()
}
```

### Bind Mounts

```kotlin
// Mount host directory
docker.run("nginx:alpine") {
    name("web")
    volume("/host/path/html", "/usr/share/nginx/html")
    volume("/host/path/config", "/etc/nginx/conf.d", "ro")
    detach()
}
```

## Common Patterns

### Persistent Database Storage

```kotlin
// Create dedicated volume for database
VolumeCreateCommand()
    .name("mysql-data")
    .label("app", "mysql")
    .label("environment", "production")
    .execute()

// Run MySQL with persistent storage
docker.run("mysql:8") {
    name("mysql")
    namedVolume("mysql-data", "/var/lib/mysql")
    env("MYSQL_ROOT_PASSWORD", "secret")
    detach()
}
```

### Share Data Between Containers

```kotlin
// Create shared volume
VolumeCreateCommand()
    .name("shared-data")
    .execute()

// Producer container
docker.run("producer-app:latest") {
    name("producer")
    namedVolume("shared-data", "/data")
    detach()
}

// Consumer container
docker.run("consumer-app:latest") {
    name("consumer")
    namedVolume("shared-data", "/data")
    detach()
}
```

### Backup Volume Data

```kotlin
// Run temporary container to backup volume
docker.run("alpine") {
    namedVolume("postgres-data", "/data")
    volume("/backup", "/backup")
    command("tar", "czf", "/backup/postgres-backup.tar.gz", "-C", "/data", ".")
    rm()
}
```

### Restore Volume Data

```kotlin
// Create new volume
VolumeCreateCommand()
    .name("postgres-data-restored")
    .execute()

// Restore from backup
docker.run("alpine") {
    namedVolume("postgres-data-restored", "/data")
    volume("/backup", "/backup")
    command("tar", "xzf", "/backup/postgres-backup.tar.gz", "-C", "/data")
    rm()
}
```

### Clean Up Unused Volumes

```kotlin
// Find dangling volumes
val danglingVolumes = VolumeLsCommand()
    .filter("dangling=true")
    .execute()

println("Found ${danglingVolumes.size} unused volumes")

// Remove them
VolumePruneCommand()
    .force()
    .execute()
```

### NFS Volume

```kotlin
VolumeCreateCommand()
    .name("nfs-share")
    .driver("local")
    .opt("type", "nfs")
    .opt("o", "addr=192.168.1.100,vers=4,soft,timeo=180,bg,tcp,rw")
    .opt("device", ":/export/share")
    .execute()
```
