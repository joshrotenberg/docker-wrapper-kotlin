package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to view output from containers.
 *
 * Equivalent to `docker compose logs`.
 *
 * Example usage:
 * ```kotlin
 * val logs = ComposeLogsCommand()
 *     .file("docker-compose.yml")
 *     .follow()
 *     .tail(100)
 *     .services("web", "api")
 *     .execute()
 * ```
 */
class ComposeLogsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<String, ComposeLogsCommand>(executor) {

    private var follow = false
    private var noColor = false
    private var noLogPrefix = false
    private var since: String? = null
    private var tail: Int? = null
    private var timestamps = false
    private var until: String? = null
    private val services = mutableListOf<String>()

    /** Follow log output. */
    fun follow() = apply { follow = true }

    /** Produce monochrome output. */
    fun noColor() = apply { noColor = true }

    /** Don't print prefix in logs. */
    fun noLogPrefix() = apply { noLogPrefix = true }

    /** Show logs since timestamp (e.g. 2021-01-01T00:00:00Z) or relative (e.g. 42m). */
    fun since(since: String) = apply { this.since = since }

    /** Number of lines to show from the end of logs. */
    fun tail(lines: Int) = apply { tail = lines }

    /** Show timestamps. */
    fun timestamps() = apply { timestamps = true }

    /** Show logs before timestamp (e.g. 2021-01-01T00:00:00Z) or relative (e.g. 42m). */
    fun until(until: String) = apply { this.until = until }

    /** Specify services to show logs for. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "logs"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (follow) add("--follow")
        if (noColor) add("--no-color")
        if (noLogPrefix) add("--no-log-prefix")
        since?.let { add("--since"); add(it) }
        tail?.let { add("--tail"); add(it.toString()) }
        if (timestamps) add("--timestamps")
        until?.let { add("--until"); add(it) }
        addAll(services)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): ComposeLogsCommand = ComposeLogsCommand()
    }
}
