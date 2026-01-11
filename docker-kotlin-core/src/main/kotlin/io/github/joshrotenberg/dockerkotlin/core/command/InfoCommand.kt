package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to display system-wide information.
 *
 * Equivalent to `docker info`.
 *
 * Example usage:
 * ```kotlin
 * val info = InfoCommand().execute()
 * val serverVersion = InfoCommand().format("{{.ServerVersion}}").execute()
 * ```
 */
class InfoCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var format: String? = null

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("info")
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
        fun builder(): InfoCommand = InfoCommand()
    }
}
