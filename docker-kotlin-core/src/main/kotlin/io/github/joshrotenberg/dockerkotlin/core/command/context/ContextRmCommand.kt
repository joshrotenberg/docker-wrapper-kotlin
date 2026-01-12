package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more Docker contexts.
 *
 * Equivalent to `docker context rm`.
 *
 * Example usage:
 * ```kotlin
 * ContextRmCommand()
 *     .contexts("old-context", "unused-context")
 *     .force()
 *     .execute()
 * ```
 */
class ContextRmCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private val contexts = mutableListOf<String>()
    private var force = false

    /** Add a context to remove. */
    fun context(name: String) = apply { contexts.add(name) }

    /** Add multiple contexts to remove. */
    fun contexts(vararg names: String) = apply { contexts.addAll(names) }

    /** Force removal of a context in use. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("context")
        add("rm")
        if (force) add("--force")
        addAll(contexts)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): ContextRmCommand = ContextRmCommand()

        @JvmStatic
        fun builder(context: String): ContextRmCommand = ContextRmCommand().context(context)
    }
}
