package io.github.joshrotenberg.dockerkotlin.core.command.stack

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list stacks.
 *
 * Equivalent to `docker stack ls`.
 *
 * Example usage:
 * ```kotlin
 * StackLsCommand().executeBlocking()
 * StackLsCommand().format("{{.Name}}").executeBlocking()
 * ```
 */
class StackLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var format: String? = null

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("stack")
        add("ls")
        format?.let { add("--format"); add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): StackLsCommand = StackLsCommand()
    }
}
