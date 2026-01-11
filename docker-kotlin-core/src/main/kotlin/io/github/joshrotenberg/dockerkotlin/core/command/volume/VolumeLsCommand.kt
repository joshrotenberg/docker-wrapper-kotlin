package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list Docker volumes.
 *
 * Equivalent to `docker volume ls`.
 *
 * Example usage:
 * ```kotlin
 * VolumeLsCommand()
 *     .driverFilter("local")
 *     .executeBlocking()
 * ```
 */
class VolumeLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var quiet = false

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by driver. */
    fun driverFilter(driver: String) = apply { filter("driver", driver) }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Filter by name. */
    fun nameFilter(name: String) = apply { filter("name", name) }

    /** Filter dangling volumes. */
    fun danglingFilter(dangling: Boolean = true) = apply { filter("dangling", dangling.toString()) }

    /** Set output format. */
    fun format(format: String) = apply { this.format = format }

    /** Format output as JSON. */
    fun formatJson() = apply { format = "json" }

    /** Only display volume names. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("ls")

        filters.forEach { (key, value) ->
            add("--filter")
            add("$key=$value")
        }

        format?.let { add("--format"); add(it) }
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
        fun builder(): VolumeLsCommand = VolumeLsCommand()
    }
}
