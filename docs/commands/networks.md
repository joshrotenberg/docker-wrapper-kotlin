# Network Commands

Commands for managing Docker networks.

## NetworkCreateCommand

Create a new network.

=== "Kotlin"

    ```kotlin
    // Create with defaults
    val networkId = NetworkCreateCommand("my-network")
        .execute()
    
    // Create with options
    NetworkCreateCommand("my-network")
        .driver("bridge")              // network driver
        .subnet("172.20.0.0/16")       // subnet CIDR
        .gateway("172.20.0.1")         // gateway address
        .ipRange("172.20.10.0/24")     // allocatable IP range
        .internal()                    // restrict external access
        .attachable()                  // allow manual container attachment
        .label("environment", "dev")
        .execute()
    ```

=== "Java"

    ```java
    String networkId = NetworkCreateCommand.builder("my-network")
        .driver("bridge")
        .subnet("172.20.0.0/16")
        .gateway("172.20.0.1")
        .executeBlocking();
    ```

### Network Drivers

| Driver | Description |
|--------|-------------|
| `bridge` | Default driver, isolated network on single host |
| `host` | Remove network isolation, use host networking |
| `overlay` | Multi-host networks (Swarm mode) |
| `macvlan` | Assign MAC address to container |
| `none` | Disable networking |

## NetworkLsCommand

List networks.

```kotlin
val networks = NetworkLsCommand()
    .execute()

networks.forEach { network ->
    println("${network.name}: ${network.driver} (${network.scope})")
}

// Filter networks
NetworkLsCommand()
    .filter("driver=bridge")
    .filter("scope=local")
    .execute()
```

## NetworkInspectCommand

Get detailed network information.

```kotlin
val network = NetworkInspectCommand("my-network")
    .execute()

println("Name: ${network.name}")
println("Driver: ${network.driver}")
println("Subnet: ${network.subnet}")
println("Gateway: ${network.gateway}")

// Show connected containers
network.containers.forEach { (containerId, info) ->
    println("Container $containerId: ${info.ipAddress}")
}
```

## NetworkConnectCommand

Connect a container to a network.

=== "Kotlin"

    ```kotlin
    // Basic connection
    NetworkConnectCommand("my-network", "my-container")
        .execute()
    
    // With options
    NetworkConnectCommand("my-network", "my-container")
        .ip("172.20.0.100")           // specific IP address
        .alias("web")                 // network alias
        .alias("frontend")            // multiple aliases
        .execute()
    ```

=== "Java"

    ```java
    NetworkConnectCommand.builder("my-network", "my-container")
        .ip("172.20.0.100")
        .alias("web")
        .executeBlocking();
    ```

## NetworkDisconnectCommand

Disconnect a container from a network.

```kotlin
NetworkDisconnectCommand("my-network", "my-container")
    .force()  // force disconnect even if container is running
    .execute()
```

## NetworkRmCommand

Remove one or more networks.

```kotlin
// Remove single network
NetworkRmCommand("my-network")
    .execute()

// Remove multiple networks
NetworkRmCommand("network1", "network2", "network3")
    .execute()
```

## NetworkPruneCommand

Remove all unused networks.

```kotlin
val removed = NetworkPruneCommand()
    .force()  // don't prompt for confirmation
    .execute()

println("Removed networks: ${removed.networks}")
```

## Common Patterns

### Create Isolated Network for Services

```kotlin
// Create network
val networkId = NetworkCreateCommand("app-network")
    .driver("bridge")
    .internal()  // no external access
    .execute()

// Run containers on the network
docker.run("postgres:16") {
    name("db")
    network("app-network")
    networkAlias("database")
    detach()
}

docker.run("my-app:latest") {
    name("app")
    network("app-network")
    env("DATABASE_HOST", "database")  // uses network alias
    detach()
}
```

### Connect Container to Multiple Networks

```kotlin
// Create container on first network
docker.run("nginx:alpine") {
    name("web")
    network("frontend")
    detach()
}

// Connect to additional network
NetworkConnectCommand("backend", "web")
    .alias("nginx")
    .execute()
```

### Inspect Network Connectivity

```kotlin
val network = NetworkInspectCommand("my-network")
    .execute()

println("Connected containers:")
network.containers.forEach { (id, info) ->
    println("  ${info.name}: ${info.ipAddress}")
}
```

### Clean Up Unused Networks

```kotlin
// Remove all unused networks
NetworkPruneCommand()
    .force()
    .execute()
```

### Custom Subnet Configuration

```kotlin
NetworkCreateCommand("custom-network")
    .driver("bridge")
    .subnet("10.10.0.0/24")
    .gateway("10.10.0.1")
    .ipRange("10.10.0.128/25")  // allocate from 10.10.0.128-255
    .execute()

// Run container with specific IP
docker.run("nginx:alpine") {
    name("web")
    network("custom-network")
    arg("--ip")
    arg("10.10.0.100")
    detach()
}
```
