package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more nodes.
 *
 * Equivalent to `docker node inspect`.
 *
 * Example usage:
 * ```kotlin
 * NodeInspectCommand("node1").executeBlocking()
 * NodeInspectCommand("node1").pretty().executeBlocking()
 * NodeInspectCommand("self").format("{{.ID}}").executeBlocking()
 * ```
 */
class NodeInspectCommand : AbstractDockerCommand<String> {

    private val nodes: List<String>
    private var format: String? = null
    private var pretty = false

    constructor(node: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = listOf(node)
    }

    constructor(nodes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = nodes
    }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Print the information in a human friendly format. */
    fun pretty() = apply { pretty = true }

    override fun buildArgs(): List<String> = buildList {
        add("node")
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (pretty) add("--pretty")
        addAll(nodes)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(node: String): NodeInspectCommand = NodeInspectCommand(node)

        @JvmStatic
        fun builder(nodes: List<String>): NodeInspectCommand = NodeInspectCommand(nodes)
    }
}
