package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to display the running processes of a container.
 *
 * Equivalent to `docker top`.
 *
 * Example usage:
 * ```kotlin
 * val processes = TopCommand("my-container").execute()
 * val processes = TopCommand("my-container").psOptions("-aux").execute()
 * ```
 */
class TopCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var psOptions: String? = null

    /** ps options (default: -ef). */
    fun psOptions(options: String) = apply { this.psOptions = options }

    override fun buildArgs(): List<String> = buildList {
        add("top")
        add(container)
        psOptions?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(container: String): TopCommand = TopCommand(container)
    }
}
