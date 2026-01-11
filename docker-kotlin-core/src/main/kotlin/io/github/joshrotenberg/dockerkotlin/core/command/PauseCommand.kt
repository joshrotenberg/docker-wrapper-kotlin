package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to pause all processes within one or more containers.
 *
 * Equivalent to `docker pause`.
 *
 * Example usage:
 * ```kotlin
 * PauseCommand("my-container").execute()
 * ```
 */
class PauseCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    override fun buildArgs(): List<String> = buildList {
        add("pause")
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
        fun builder(container: String): PauseCommand = PauseCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): PauseCommand = PauseCommand(containers)
    }
}
