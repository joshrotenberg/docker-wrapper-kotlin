package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to fetch the logs of a service or task.
 *
 * Equivalent to `docker service logs`.
 *
 * Example usage:
 * ```kotlin
 * ServiceLogsCommand("my-service").executeBlocking()
 * ServiceLogsCommand("my-service").tail(100).timestamps().executeBlocking()
 * ServiceLogsCommand("my-service").follow().executeBlocking()
 * ```
 */
class ServiceLogsCommand(
    private val service: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var details = false
    private var follow = false
    private var noResolve = false
    private var noTaskIds = false
    private var noTrunc = false
    private var raw = false
    private var since: String? = null
    private var tail: String? = null
    private var timestamps = false

    /** Show extra details provided to logs. */
    fun details() = apply { details = true }

    /** Follow log output. */
    fun follow() = apply { follow = true }

    /** Do not map IDs to names. */
    fun noResolve() = apply { noResolve = true }

    /** Do not include task IDs. */
    fun noTaskIds() = apply { noTaskIds = true }

    /** Do not truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Do not neatly format logs. */
    fun raw() = apply { raw = true }

    /** Show logs since timestamp or relative (e.g. 42m for 42 minutes). */
    fun since(since: String) = apply { this.since = since }

    /** Number of lines to show from the end of the logs. */
    fun tail(lines: Int) = apply { tail = lines.toString() }

    /** Show all logs (don't tail). */
    fun tailAll() = apply { tail = "all" }

    /** Show timestamps. */
    fun timestamps() = apply { timestamps = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("logs")
        if (details) add("--details")
        if (follow) add("--follow")
        if (noResolve) add("--no-resolve")
        if (noTaskIds) add("--no-task-ids")
        if (noTrunc) add("--no-trunc")
        if (raw) add("--raw")
        since?.let { add("--since"); add(it) }
        tail?.let { add("--tail"); add(it) }
        if (timestamps) add("--timestamps")
        add(service)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(service: String): ServiceLogsCommand = ServiceLogsCommand(service)
    }
}
