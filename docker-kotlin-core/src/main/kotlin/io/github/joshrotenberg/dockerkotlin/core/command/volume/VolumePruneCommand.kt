package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove all unused local volumes.
 *
 * Equivalent to `docker volume prune`.
 *
 * Example usage:
 * ```kotlin
 * VolumePruneCommand().force().executeBlocking()
 * VolumePruneCommand().all().force().executeBlocking()
 * ```
 */
class VolumePruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var all = false
    private val filters = mutableMapOf<String, String>()
    private var force = false

    /** Remove all unused volumes, not just anonymous ones. */
    fun all() = apply { all = true }

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Do not prompt for confirmation. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("prune")

        if (all) add("--all")

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
        fun builder(): VolumePruneCommand = VolumePruneCommand()
    }
}
