package io.github.joshrotenberg.dockerkotlin.core.command.stack

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to output the final config file, after doing merges and interpolations.
 *
 * Equivalent to `docker stack config`.
 *
 * Example usage:
 * ```kotlin
 * StackConfigCommand("docker-compose.yml").executeBlocking()
 * StackConfigCommand(listOf("docker-compose.yml", "docker-compose.prod.yml")).executeBlocking()
 * ```
 */
class StackConfigCommand : AbstractDockerCommand<String> {

    private val composeFiles: List<String>
    private var skipInterpolation = false

    constructor(composeFile: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.composeFiles = listOf(composeFile)
    }

    constructor(composeFiles: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.composeFiles = composeFiles
    }

    /** Skip interpolation and output only merged config. */
    fun skipInterpolation() = apply { skipInterpolation = true }

    override fun buildArgs(): List<String> = buildList {
        add("stack")
        add("config")
        composeFiles.forEach { add("--compose-file"); add(it) }
        if (skipInterpolation) add("--skip-interpolation")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(composeFile: String): StackConfigCommand = StackConfigCommand(composeFile)

        @JvmStatic
        fun builder(composeFiles: List<String>): StackConfigCommand = StackConfigCommand(composeFiles)
    }
}
