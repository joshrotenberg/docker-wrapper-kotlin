package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to update a Docker context.
 *
 * Equivalent to `docker context update`.
 *
 * Example usage:
 * ```kotlin
 * ContextUpdateCommand("my-context")
 *     .description("Updated description")
 *     .dockerHost("tcp://newserver:2376")
 *     .execute()
 * ```
 */
class ContextUpdateCommand(
    private val name: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var description: String? = null
    private val dockerOptions = mutableMapOf<String, String>()

    /** Set the description of the context. */
    fun description(description: String) = apply { this.description = description }

    /** Set the Docker host endpoint. */
    fun dockerHost(host: String) = apply { dockerOptions["host"] = host }

    /** Set the CA certificate path. */
    fun ca(path: String) = apply { dockerOptions["ca"] = path }

    /** Set the TLS certificate path. */
    fun cert(path: String) = apply { dockerOptions["cert"] = path }

    /** Set the TLS key path. */
    fun key(path: String) = apply { dockerOptions["key"] = path }

    /** Skip TLS certificate validation. */
    fun skipTlsVerify() = apply { dockerOptions["skip-tls-verify"] = "true" }

    /** Set a raw Docker endpoint option. */
    fun dockerOption(key: String, value: String) = apply { dockerOptions[key] = value }

    override fun buildArgs(): List<String> = buildList {
        add("context")
        add("update")
        description?.let { add("--description"); add(it) }
        if (dockerOptions.isNotEmpty()) {
            add("--docker")
            add(dockerOptions.entries.joinToString(",") { "${it.key}=${it.value}" })
        }
        add(name)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(name: String): ContextUpdateCommand = ContextUpdateCommand(name)
    }
}
