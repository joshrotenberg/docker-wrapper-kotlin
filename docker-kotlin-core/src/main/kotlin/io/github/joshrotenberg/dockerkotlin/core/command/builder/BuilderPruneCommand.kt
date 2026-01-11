package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove build cache.
 *
 * Equivalent to `docker builder prune` / `docker buildx prune`.
 *
 * Example usage:
 * ```kotlin
 * BuilderPruneCommand().force().executeBlocking()
 * BuilderPruneCommand().all().keepStorage("10GB").executeBlocking()
 * ```
 */
class BuilderPruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var all = false
    private var force = false
    private var keepStorage: String? = null
    private val filters = mutableListOf<String>()
    private var verbose = false

    /** Remove all unused build cache, not just dangling ones. */
    fun all() = apply { all = true }

    /** Do not prompt for confirmation. */
    fun force() = apply { force = true }

    /** Amount of disk space to keep for cache (e.g., "10GB"). */
    fun keepStorage(size: String) = apply { keepStorage = size }

    /** Add a filter (e.g., "until=24h", "type=regular"). */
    fun filter(filter: String) = apply { filters.add(filter) }

    /** Filter by duration (e.g., "24h", "7d"). */
    fun until(duration: String) = filter("until=$duration")

    /** Provide verbose output. */
    fun verbose() = apply { verbose = true }

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("prune")

        if (all) add("--all")
        if (force) add("--force")

        keepStorage?.let { add("--keep-storage"); add(it) }

        filters.forEach {
            add("--filter")
            add(it)
        }

        if (verbose) add("--verbose")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderPruneCommand = BuilderPruneCommand()
    }
}
