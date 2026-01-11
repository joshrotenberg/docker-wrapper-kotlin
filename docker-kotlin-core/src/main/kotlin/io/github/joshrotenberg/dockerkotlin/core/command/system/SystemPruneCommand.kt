package io.github.joshrotenberg.dockerkotlin.core.command.system

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove unused Docker data.
 *
 * Removes:
 * - All stopped containers
 * - All networks not used by at least one container
 * - All dangling images
 * - All dangling build cache
 * - Optionally all volumes not used by at least one container
 *
 * Equivalent to `docker system prune`.
 *
 * Example usage:
 * ```kotlin
 * SystemPruneCommand().force().executeBlocking()
 * SystemPruneCommand().all().volumes().force().executeBlocking()
 * ```
 */
class SystemPruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var all = false
    private var volumes = false
    private var force = false
    private val filters = mutableMapOf<String, String>()

    /** Remove all unused images, not just dangling ones. */
    fun all() = apply { all = true }

    /** Prune volumes as well. */
    fun volumes() = apply { volumes = true }

    /** Do not prompt for confirmation. */
    fun force() = apply { force = true }

    /** Add a filter (e.g., "until=24h", "label=foo=bar"). */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Prune objects older than the specified duration. */
    fun until(duration: String) = apply { filter("until", duration) }

    override fun buildArgs(): List<String> = buildList {
        add("system")
        add("prune")

        if (all) add("--all")
        if (volumes) add("--volumes")
        if (force) add("--force")

        filters.forEach { (key, value) ->
            add("--filter")
            add("$key=$value")
        }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): SystemPruneCommand = SystemPruneCommand()
    }
}
