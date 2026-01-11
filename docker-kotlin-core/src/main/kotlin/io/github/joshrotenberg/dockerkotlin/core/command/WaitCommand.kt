package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to block until one or more containers stop, then print their exit codes.
 *
 * Equivalent to `docker wait`.
 *
 * Example usage:
 * ```kotlin
 * val exitCodes = WaitCommand("my-container").execute()
 * ```
 */
class WaitCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<Int>>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    override fun buildArgs(): List<String> = buildList {
        add("wait")
        addAll(containers)
    }

    override suspend fun execute(): List<Int> {
        val output = executeRaw()
        return output.stdout.lines()
            .filter { it.isNotBlank() }
            .map { it.trim().toInt() }
    }

    override fun executeBlocking(): List<Int> {
        val output = executeRawBlocking()
        return output.stdout.lines()
            .filter { it.isNotBlank() }
            .map { it.trim().toInt() }
    }

    companion object {
        @JvmStatic
        fun builder(container: String): WaitCommand = WaitCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): WaitCommand = WaitCommand(containers)
    }
}
