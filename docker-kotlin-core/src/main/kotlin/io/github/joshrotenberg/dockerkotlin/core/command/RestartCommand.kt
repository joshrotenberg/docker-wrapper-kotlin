package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to restart one or more containers.
 *
 * Equivalent to `docker restart`.
 *
 * Example usage:
 * ```kotlin
 * RestartCommand("my-container").execute()
 * ```
 */
class RestartCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    private var time: Int? = null
    private var signal: String? = null

    /** Seconds to wait before killing the container (default 10). */
    fun time(seconds: Int) = apply { this.time = seconds }

    /** Signal to send to the container. */
    fun signal(signal: String) = apply { this.signal = signal }

    override fun buildArgs(): List<String> = buildList {
        add("restart")
        time?.let { add("--time"); add(it.toString()) }
        signal?.let { add("--signal"); add(it) }
        addAll(containers)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): RestartCommand = RestartCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): RestartCommand = RestartCommand(containers)
    }
}
