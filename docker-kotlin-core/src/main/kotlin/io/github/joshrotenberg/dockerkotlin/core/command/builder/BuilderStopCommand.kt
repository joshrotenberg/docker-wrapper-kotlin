package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException

/**
 * Command to stop a builder instance.
 *
 * Equivalent to `docker builder stop` / `docker buildx stop`.
 *
 * Note: This command is not supported by Podman, which uses buildah
 * and does not have the concept of multiple builder instances.
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
        checkRuntimeSupport()
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        checkRuntimeSupport()
        return executeRawBlocking().stdout
    }

    private fun checkRuntimeSupport() {
        if (!executor.supportsBuilderCommand("stop")) {
            throw DockerException.UnsupportedByRuntime(
                command = "builder stop",
                runtime = executor.runtime?.name ?: "unknown"
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderStopCommand = BuilderStopCommand()

        @JvmStatic
        fun builder(name: String): BuilderStopCommand = BuilderStopCommand(name)
    }
}
