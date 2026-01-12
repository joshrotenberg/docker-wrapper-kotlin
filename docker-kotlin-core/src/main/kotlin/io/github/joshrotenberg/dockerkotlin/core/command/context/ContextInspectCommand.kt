package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more contexts.
 *
 * Equivalent to `docker context inspect`.
 *
 * Example usage:
 * ```kotlin
 * val json = ContextInspectCommand("my-context")
 *     .execute()
 * ```
 */
class ContextInspectCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val contexts = mutableListOf<String>()
    private var format: String? = null

    /** Add a context to inspect. */
    fun context(name: String) = apply { contexts.add(name) }

    /** Add multiple contexts to inspect. */
    fun contexts(vararg names: String) = apply { contexts.addAll(names) }

    /** Format output using a Go template or 'json'. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("context")
        add("inspect")
        format?.let { add("--format"); add(it) }
        addAll(contexts)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): ContextInspectCommand = ContextInspectCommand()

        @JvmStatic
        fun builder(context: String): ContextInspectCommand = ContextInspectCommand().context(context)
    }
}
