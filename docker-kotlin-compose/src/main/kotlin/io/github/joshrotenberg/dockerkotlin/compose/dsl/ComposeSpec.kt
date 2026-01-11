package io.github.joshrotenberg.dockerkotlin.compose.dsl

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.StringWriter

/**
 * Root specification for a Docker Compose file.
 *
 * Example usage:
 * ```kotlin
 * val compose = dockerCompose {
 *     service("web") {
 *         image = "nginx:alpine"
 *         ports("8080:80")
 *         dependsOn("redis")
 *     }
 *
 *     service("redis") {
 *         image = "redis:7-alpine"
 *     }
 * }
 *
 * // Generate YAML
 * println(compose.toYaml())
 *
 * // Write to file
 * compose.writeTo(File("docker-compose.yml"))
 * ```
 */
class ComposeSpec {
    /** Compose file format version. */
    var version: String? = null

    /** Service name for single-service compose files. */
    var name: String? = null

    internal val services = mutableMapOf<String, ServiceSpec>()
    internal val networks = mutableMapOf<String, NetworkSpec>()
    internal val volumes = mutableMapOf<String, VolumeSpec>()
    internal val configs = mutableMapOf<String, ConfigSpec>()
    internal val secrets = mutableMapOf<String, SecretSpec>()

    /**
     * Define a service.
     */
    fun service(name: String, init: ServiceSpec.() -> Unit) {
        services[name] = ServiceSpec().apply(init)
    }

    /**
     * Define a network.
     */
    fun network(name: String, init: NetworkSpec.() -> Unit = {}) {
        networks[name] = NetworkSpec().apply(init)
    }

    /**
     * Define a volume.
     */
    fun volume(name: String, init: VolumeSpec.() -> Unit = {}) {
        volumes[name] = VolumeSpec().apply(init)
    }

    /**
     * Define a config.
     */
    fun config(name: String, init: ConfigSpec.() -> Unit) {
        configs[name] = ConfigSpec().apply(init)
    }

    /**
     * Define a secret.
     */
    fun secret(name: String, init: SecretSpec.() -> Unit) {
        secrets[name] = SecretSpec().apply(init)
    }

    /**
     * Convert to a Map suitable for YAML serialization.
     */
    fun toMap(): Map<String, Any> = buildMap {
        version?.let { put("version", it) }
        name?.let { put("name", it) }

        if (services.isNotEmpty()) {
            put("services", services.mapValues { it.value.toMap() })
        }

        if (networks.isNotEmpty()) {
            put("networks", networks.mapValues { it.value.toMap() })
        }

        if (volumes.isNotEmpty()) {
            put("volumes", volumes.mapValues { it.value.toMap() })
        }

        if (configs.isNotEmpty()) {
            put("configs", configs.mapValues { it.value.toMap() })
        }

        if (secrets.isNotEmpty()) {
            put("secrets", secrets.mapValues { it.value.toMap() })
        }
    }

    /**
     * Generate YAML string.
     */
    fun toYaml(): String {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
            indent = 2
            indicatorIndent = 0
        }
        val yaml = Yaml(options)
        val writer = StringWriter()
        yaml.dump(toMap(), writer)
        return writer.toString()
    }

    /**
     * Write YAML to a file.
     */
    fun writeTo(file: File) {
        file.writeText(toYaml())
    }

    /**
     * Write YAML to a file at the given path.
     */
    fun writeTo(path: String) {
        writeTo(File(path))
    }
}

/**
 * DSL entry point for creating a Docker Compose specification.
 */
fun dockerCompose(init: ComposeSpec.() -> Unit): ComposeSpec {
    return ComposeSpec().apply(init)
}
