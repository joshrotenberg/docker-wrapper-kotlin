package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list Docker networks.
 *
 * Equivalent to `docker network ls`.
 *
 * Example usage:
 * ```kotlin
 * NetworkLsCommand()
 *     .driverFilter("bridge")
 *     .executeBlocking()
 * ```
 */
class NetworkLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var noTrunc = false
    private var quiet = false

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by driver. */
    fun driverFilter(driver: String) = apply { filter("driver", driver) }

    /** Filter by ID. */
    fun idFilter(id: String) = apply { filter("id", id) }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Filter by name. */
    fun nameFilter(name: String) = apply { filter("name", name) }

    /** Filter by scope. */
    fun scopeFilter(scope: String) = apply { filter("scope", scope) }

    /** Filter by type (custom or builtin). */
    fun typeFilter(type: String) = apply { filter("type", type) }

    /** Set output format. */
    fun format(format: String) = apply { this.format = format }

    /** Format output as JSON. */
    fun formatJson() = apply { format = "json" }

    /** Don't truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Only display network IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("ls")

        filters.forEach { (key, value) ->
            add("--filter")
            add("$key=$value")
        }

        format?.let { add("--format"); add(it) }
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): NetworkLsCommand = NetworkLsCommand()
    }
}
