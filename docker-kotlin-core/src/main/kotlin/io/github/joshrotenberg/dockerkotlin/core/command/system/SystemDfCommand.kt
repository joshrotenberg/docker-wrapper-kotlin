package io.github.joshrotenberg.dockerkotlin.core.command.system

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to show Docker disk usage.
 *
 * Equivalent to `docker system df`.
 *
 * Example usage:
 * ```kotlin
 * SystemDfCommand().executeBlocking()
 * SystemDfCommand().verbose().executeBlocking()
 * ```
 */
class SystemDfCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var verbose = false
    private var format: String? = null

    /** Show detailed information on space usage. */
    fun verbose() = apply { verbose = true }

    /** Format output using a custom template. */
    fun format(format: String) = apply { this.format = format }

    /** Format output as JSON. */
    fun formatJson() = apply { format = "json" }

    override fun buildArgs(): List<String> = buildList {
        add("system")
        add("df")
        if (verbose) add("--verbose")
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
        fun builder(): SystemDfCommand = SystemDfCommand()
    }
}
