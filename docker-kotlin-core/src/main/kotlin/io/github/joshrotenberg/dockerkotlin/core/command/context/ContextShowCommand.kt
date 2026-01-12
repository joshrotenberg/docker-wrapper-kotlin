package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to print the name of the current Docker context.
 *
 * Equivalent to `docker context show`.
 *
 * Example usage:
 * ```kotlin
 * val currentContext = ContextShowCommand()
 *     .execute()
 * println("Current context: $currentContext")
 * ```
 */
class ContextShowCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    override fun buildArgs(): List<String> = listOf("context", "show")

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(): ContextShowCommand = ContextShowCommand()
    }
}
