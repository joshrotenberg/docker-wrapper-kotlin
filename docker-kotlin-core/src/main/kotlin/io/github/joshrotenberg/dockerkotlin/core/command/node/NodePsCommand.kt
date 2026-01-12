package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list tasks running on one or more nodes.
 *
 * Equivalent to `docker node ps`.
 *
 * Example usage:
 * ```kotlin
 * NodePsCommand().executeBlocking()  // defaults to self
 * NodePsCommand("node1").executeBlocking()
 * NodePsCommand(listOf("node1", "node2")).filter("desired-state", "running").executeBlocking()
 * ```
 */
class NodePsCommand : AbstractDockerCommand<String> {

    private val nodes: List<String>
    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var noResolve = false
    private var noTrunc = false
    private var quiet = false

    constructor(executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = emptyList()
    }

    constructor(node: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = listOf(node)
    }

    constructor(nodes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = nodes
    }

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
        add("node")
        add("ps")
        filters.forEach { (key, value) -> add("--filter"); add("$key=$value") }
        format?.let { add("--format"); add(it) }
        if (noResolve) add("--no-resolve")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
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
        fun builder(): NodePsCommand = NodePsCommand()

        @JvmStatic
        fun builder(node: String): NodePsCommand = NodePsCommand(node)

        @JvmStatic
        fun builder(nodes: List<String>): NodePsCommand = NodePsCommand(nodes)
    }
}
