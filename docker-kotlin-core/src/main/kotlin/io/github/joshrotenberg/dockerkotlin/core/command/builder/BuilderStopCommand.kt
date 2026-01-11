package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to stop a builder instance.
 *
 * Equivalent to `docker builder stop` / `docker buildx stop`.
 *
 * Example usage:
 * ```kotlin
 * BuilderStopCommand("my-builder").executeBlocking()
 * BuilderStopCommand().executeBlocking() // stops current builder
 * ```
 */
class BuilderStopCommand(
    private val name: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("stop")

        name?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderStopCommand = BuilderStopCommand()

        @JvmStatic
        fun builder(name: String): BuilderStopCommand = BuilderStopCommand(name)
    }
}
