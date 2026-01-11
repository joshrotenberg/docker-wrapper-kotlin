package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to get real-time events from the server.
 *
 * Equivalent to `docker events`.
 *
 * Example usage:
 * ```kotlin
 * val events = EventsCommand()
 *     .since("1h")
 *     .until("now")
 *     .filterType("container")
 *     .execute()
 * ```
 */
class EventsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableListOf<String>()
    private var format: String? = null
    private var since: String? = null
    private var until: String? = null

    /** Filter output based on conditions. */
    fun filter(filter: String) = apply { this.filters.add(filter) }

    /** Filter by container name or ID. */
    fun filterContainer(container: String) = filter("container=$container")

    /** Filter by event type (container, image, volume, network, daemon, plugin, node, service, secret, config). */
    fun filterType(type: String) = filter("type=$type")

    /** Filter by event (start, stop, create, destroy, etc.). */
    fun filterEvent(event: String) = filter("event=$event")

    /** Filter by image name. */
    fun filterImage(image: String) = filter("image=$image")

    /** Filter by label. */
    fun filterLabel(label: String) = filter("label=$label")

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Show events since timestamp. */
    fun since(since: String) = apply { this.since = since }

    /** Stream events until this timestamp. */
    fun until(until: String) = apply { this.until = until }

    override fun buildArgs(): List<String> = buildList {
        add("events")
        filters.forEach { add("--filter"); add(it) }
        format?.let { add("--format"); add(it) }
        since?.let { add("--since"); add(it) }
        until?.let { add("--until"); add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): EventsCommand = EventsCommand()
    }
}
