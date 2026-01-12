package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to export a Docker context to a tar archive.
 *
 * Equivalent to `docker context export`.
 *
 * Example usage:
 * ```kotlin
 * ContextExportCommand("my-context")
 *     .output("my-context.tar")
 *     .execute()
 * ```
 */
class ContextExportCommand(
    private val name: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var outputFile: String? = null

    /** Set the output file path (if not set, outputs to stdout). */
    fun output(file: String) = apply { outputFile = file }

    override fun buildArgs(): List<String> = buildList {
        add("context")
        add("export")
        add(name)
        outputFile?.let { add(it) }
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(name: String): ContextExportCommand = ContextExportCommand(name)
    }
}
