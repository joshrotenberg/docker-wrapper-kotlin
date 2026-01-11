package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException

/**
 * Command to list builder instances.
 *
 * Equivalent to `docker builder ls` / `docker buildx ls`.
 *
 * Note: This command is not supported by Podman, which uses buildah
 * and does not have the concept of multiple builder instances.
 *
 * Example usage:
 * ```kotlin
 * val builders = BuilderLsCommand().executeBlocking()
 * ```
 */
class BuilderLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var format: String? = null

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Output as JSON. */
    fun json() = format("json")

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("ls")

        format?.let { add("--format"); add(it) }
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
        if (!executor.supportsBuilderCommand("ls")) {
            throw DockerException.UnsupportedByRuntime(
                command = "builder ls",
                runtime = executor.runtime?.name ?: "unknown"
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderLsCommand = BuilderLsCommand()
    }
}
