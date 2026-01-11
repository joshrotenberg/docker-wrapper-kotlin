package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to export a container's filesystem as a tar archive.
 *
 * Equivalent to `docker export`.
 *
 * Example usage:
 * ```kotlin
 * ExportCommand("my-container").output("/tmp/container.tar").execute()
 * ```
 */
class ExportCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var output: String? = null

    /** Write to a file, instead of STDOUT. */
    fun output(path: String) = apply { this.output = path }

    override fun buildArgs(): List<String> = buildList {
        add("export")
        output?.let { add("--output"); add(it) }
        add(container)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): ExportCommand = ExportCommand(container)
    }
}
