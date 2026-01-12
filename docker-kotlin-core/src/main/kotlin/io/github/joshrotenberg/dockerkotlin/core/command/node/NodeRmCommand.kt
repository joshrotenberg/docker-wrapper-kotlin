package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more nodes from the swarm.
 *
 * Equivalent to `docker node rm`.
 *
 * Example usage:
 * ```kotlin
 * NodeRmCommand("node1").executeBlocking()
 * NodeRmCommand("node1").force().executeBlocking()
 * NodeRmCommand(listOf("node1", "node2")).executeBlocking()
 * ```
 */
class NodeRmCommand : AbstractDockerCommand<String> {

    private val nodes: List<String>
    private var force = false

    constructor(node: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = listOf(node)
    }

    constructor(nodes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = nodes
    }

    /** Force remove a node from the swarm. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("node")
        add("rm")
        if (force) add("--force")
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
        fun builder(node: String): NodeRmCommand = NodeRmCommand(node)

        @JvmStatic
        fun builder(nodes: List<String>): NodeRmCommand = NodeRmCommand(nodes)
    }
}
