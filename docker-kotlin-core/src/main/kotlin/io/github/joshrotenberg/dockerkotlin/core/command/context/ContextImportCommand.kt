package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to import a Docker context from a tar or zip file.
 *
 * Equivalent to `docker context import`.
 *
 * Example usage:
 * ```kotlin
 * ContextImportCommand("my-context", "my-context.tar")
 *     .execute()
 * ```
 */
class ContextImportCommand(
    private val name: String,
    private val file: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    override fun buildArgs(): List<String> = listOf("context", "import", name, file)

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(name: String, file: String): ContextImportCommand = ContextImportCommand(name, file)
    }
}
