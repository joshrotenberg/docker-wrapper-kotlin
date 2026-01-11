package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to unpause all processes within one or more containers.
 *
 * Equivalent to `docker unpause`.
 *
 * Example usage:
 * ```kotlin
 * UnpauseCommand("my-container").execute()
 * ```
 */
class UnpauseCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    override fun buildArgs(): List<String> = buildList {
        add("unpause")
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
        fun builder(container: String): UnpauseCommand = UnpauseCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): UnpauseCommand = UnpauseCommand(containers)
    }
}
