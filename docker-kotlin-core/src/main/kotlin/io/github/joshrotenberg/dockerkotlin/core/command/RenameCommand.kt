package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to rename a container.
 *
 * Equivalent to `docker rename`.
 *
 * Example usage:
 * ```kotlin
 * RenameCommand("old-name", "new-name").execute()
 * ```
 */
class RenameCommand(
    private val container: String,
    private val newName: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    override fun buildArgs(): List<String> = buildList {
        add("rename")
        add(container)
        add(newName)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String, newName: String): RenameCommand =
            RenameCommand(container, newName)
    }
}
