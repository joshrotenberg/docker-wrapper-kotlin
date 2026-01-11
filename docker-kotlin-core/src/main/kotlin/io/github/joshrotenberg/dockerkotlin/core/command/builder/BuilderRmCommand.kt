package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove a builder instance.
 *
 * Equivalent to `docker builder rm` / `docker buildx rm`.
 *
 * Example usage:
 * ```kotlin
 * BuilderRmCommand("my-builder").executeBlocking()
 * BuilderRmCommand("my-builder").allInactive().force().executeBlocking()
 * ```
 */
class BuilderRmCommand(
    private val name: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var allInactive = false
    private var force = false
    private var keepState = false
    private var keepDaemon = false

    /** Remove all inactive builders. */
    fun allInactive() = apply { allInactive = true }

    /** Force remove even if the builder is in use. */
    fun force() = apply { force = true }

    /** Keep BuildKit state. */
    fun keepState() = apply { keepState = true }

    /** Keep the buildkitd daemon running. */
    fun keepDaemon() = apply { keepDaemon = true }

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("rm")

        if (allInactive) add("--all-inactive")
        if (force) add("--force")
        if (keepState) add("--keep-state")
        if (keepDaemon) add("--keep-daemon")

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
        fun builder(): BuilderRmCommand = BuilderRmCommand()

        @JvmStatic
        fun builder(name: String): BuilderRmCommand = BuilderRmCommand(name)
    }
}
