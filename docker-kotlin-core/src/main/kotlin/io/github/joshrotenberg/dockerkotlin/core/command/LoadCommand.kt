package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to load an image from a tar archive or STDIN.
 *
 * Equivalent to `docker load`.
 *
 * Example usage:
 * ```kotlin
 * LoadCommand().input("/tmp/image.tar").execute()
 * ```
 */
class LoadCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var input: String? = null
    private var quiet: Boolean = false

    /** Read from tar archive file, instead of STDIN. */
    fun input(path: String) = apply { this.input = path }

    /** Suppress the load output. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("load")
        input?.let { add("--input"); add(it) }
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): LoadCommand = LoadCommand()
    }
}
