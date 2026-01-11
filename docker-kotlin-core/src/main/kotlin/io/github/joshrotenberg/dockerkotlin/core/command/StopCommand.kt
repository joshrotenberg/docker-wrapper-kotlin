package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to stop a running container.
 *
 * Equivalent to `docker stop`.
 *
 * Example usage:
 * ```kotlin
 * StopCommand("my-container").execute()
 * ```
 */
class StopCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var time: Int? = null
    private var signal: String? = null

    /** Seconds to wait before killing the container (default 10). */
    fun time(seconds: Int) = apply { this.time = seconds }

    /** Signal to send to the container. */
    fun signal(signal: String) = apply { this.signal = signal }

    override fun buildArgs(): List<String> = buildList {
        add("stop")
        time?.let { add("--time"); add(it.toString()) }
        signal?.let { add("--signal"); add(it) }
        add(container)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): StopCommand = StopCommand(container)
    }
}
