package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to kill one or more running containers.
 *
 * Equivalent to `docker kill`.
 *
 * Example usage:
 * ```kotlin
 * KillCommand("my-container").execute()
 * KillCommand("my-container").signal("SIGKILL").execute()
 * ```
 */
class KillCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    private var signal: String? = null

    /** Signal to send to the container (default: SIGKILL). */
    fun signal(signal: String) = apply { this.signal = signal }

    override fun buildArgs(): List<String> = buildList {
        add("kill")
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
        fun builder(container: String): KillCommand = KillCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): KillCommand = KillCommand(containers)
    }
}
