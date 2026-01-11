package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to set the current builder instance.
 *
 * Equivalent to `docker builder use` / `docker buildx use`.
 *
 * Example usage:
 * ```kotlin
 * BuilderUseCommand("my-builder").executeBlocking()
 * BuilderUseCommand("my-builder").default().executeBlocking()
 * ```
 */
class BuilderUseCommand(
    private val name: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var default = false
    private var global = false

    /** Set builder as default for the current context. */
    fun default() = apply { default = true }

    /** Builder persists context changes. */
    fun global() = apply { global = true }

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("use")

        if (default) add("--default")
        if (global) add("--global")

        add(name)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(name: String): BuilderUseCommand = BuilderUseCommand(name)
    }
}
