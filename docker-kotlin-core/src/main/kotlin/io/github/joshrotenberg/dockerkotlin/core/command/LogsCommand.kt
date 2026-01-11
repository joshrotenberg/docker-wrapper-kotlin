package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to fetch the logs of a container.
 *
 * Equivalent to `docker logs`.
 *
 * Example usage:
 * ```kotlin
 * val logs = LogsCommand("my-container").execute()
 * val logs = LogsCommand("my-container").tail(100).timestamps().execute()
 * ```
 */
class LogsCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var details: Boolean = false
    private var follow: Boolean = false
    private var since: String? = null
    private var until: String? = null
    private var tail: String? = null
    private var timestamps: Boolean = false

    /** Show extra details provided to logs. */
    fun details() = apply { this.details = true }

    /** Follow log output. */
    fun follow() = apply { this.follow = true }

    /** Show logs since timestamp (e.g., "2013-01-02T13:23:37Z") or relative (e.g., "42m"). */
    fun since(since: String) = apply { this.since = since }

    /** Show logs before a timestamp or relative. */
    fun until(until: String) = apply { this.until = until }

    /** Number of lines to show from the end of the logs. */
    fun tail(n: Int) = apply { this.tail = n.toString() }

    /** Show all logs. */
    fun tailAll() = apply { this.tail = "all" }

    /** Show timestamps. */
    fun timestamps() = apply { this.timestamps = true }

    override fun buildArgs(): List<String> = buildList {
        add("logs")
        if (details) add("--details")
        if (follow) add("--follow")
        since?.let { add("--since"); add(it) }
        until?.let { add("--until"); add(it) }
        tail?.let { add("--tail"); add(it) }
        if (timestamps) add("--timestamps")
        add(container)
    }

    override suspend fun execute(): String {
        val output = executeRaw()
        return output.stdout
    }

    override fun executeBlocking(): String {
        val output = executeRawBlocking()
        return output.stdout
    }

    companion object {
        @JvmStatic
        fun builder(container: String): LogsCommand = LogsCommand(container)
    }
}
