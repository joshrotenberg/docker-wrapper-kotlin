package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to set the current Docker context.
 *
 * Equivalent to `docker context use`.
 *
 * Example usage:
 * ```kotlin
 * ContextUseCommand("my-context")
 *     .execute()
 * ```
 */
class ContextUseCommand(
    private val name: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    override fun buildArgs(): List<String> = listOf("context", "use", name)

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(name: String): ContextUseCommand = ContextUseCommand(name)
    }
}
