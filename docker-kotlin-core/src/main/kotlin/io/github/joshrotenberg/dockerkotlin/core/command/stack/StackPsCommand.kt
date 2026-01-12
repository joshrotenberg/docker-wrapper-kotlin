package io.github.joshrotenberg.dockerkotlin.core.command.stack

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list the tasks in the stack.
 *
 * Equivalent to `docker stack ps`.
 *
 * Example usage:
 * ```kotlin
 * StackPsCommand("my-stack").executeBlocking()
 * StackPsCommand("my-stack").filter("desired-state", "running").executeBlocking()
 * StackPsCommand("my-stack").noTrunc().quiet().executeBlocking()
 * ```
 */
class StackPsCommand(
    private val stack: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var noResolve = false
    private var noTrunc = false
    private var quiet = false

    /** Filter output based on conditions provided. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Do not map IDs to names. */
    fun noResolve() = apply { noResolve = true }

    /** Do not truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Only display task IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("stack")
        add("ps")
        filters.forEach { (key, value) -> add("--filter"); add("$key=$value") }
        format?.let { add("--format"); add(it) }
        if (noResolve) add("--no-resolve")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        add(stack)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(stack: String): StackPsCommand = StackPsCommand(stack)
    }
}
