package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to remove unused images.
 *
 * Equivalent to `docker image prune`.
 *
 * Example usage:
 * ```kotlin
 * ImagePruneCommand().force().executeBlocking()
 * ImagePruneCommand().all().force().executeBlocking()
 * ImagePruneCommand().until("24h").force().executeBlocking()
 * ```
 */
class ImagePruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var all = false
    private var force = false
    private val filters = mutableMapOf<String, String>()

    /** Remove all unused images, not just dangling ones. */
    fun all() = apply { all = true }

    /** Do not prompt for confirmation. */
    fun force() = apply { force = true }

    /** Add a filter (e.g., "until=24h", "label=foo=bar"). */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Prune images older than the specified duration. */
    fun until(duration: String) = apply { filter("until", duration) }

    /** Only remove dangling images (default behavior). */
    fun danglingOnly() = apply { filter("dangling", "true") }

    override fun buildArgs(): List<String> = buildList {
        add("image")
        add("prune")

        if (all) add("--all")
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
        fun builder(): ImagePruneCommand = ImagePruneCommand()
    }
}
