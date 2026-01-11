package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove all unused networks.
 *
 * Equivalent to `docker network prune`.
 *
 * Example usage:
 * ```kotlin
 * NetworkPruneCommand().force().executeBlocking()
 * NetworkPruneCommand().until("24h").force().executeBlocking()
 * ```
 */
class NetworkPruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var until: String? = null
    private val filters = mutableMapOf<String, String>()
    private var force = false

    /** Remove networks created before given timestamp. */
    fun until(timestamp: String) = apply { this.until = timestamp }

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Do not prompt for confirmation. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("prune")

        until?.let {
            add("--filter")
            add("until=$it")
        }

        filters.forEach { (key, value) ->
            add("--filter")
            add("$key=$value")
        }

        if (force) add("--force")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): NetworkPruneCommand = NetworkPruneCommand()
    }
}
