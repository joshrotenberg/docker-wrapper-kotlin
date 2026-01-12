package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to demote one or more nodes from manager in the swarm.
 *
 * Equivalent to `docker node demote`.
 *
 * Example usage:
 * ```kotlin
 * NodeDemoteCommand("node1").executeBlocking()
 * NodeDemoteCommand(listOf("node1", "node2")).executeBlocking()
 * ```
 */
class NodeDemoteCommand : AbstractDockerCommand<String> {

    private val nodes: List<String>

    constructor(node: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = listOf(node)
    }

    constructor(nodes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = nodes
    }

    override fun buildArgs(): List<String> = buildList {
        add("node")
        add("demote")
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
        fun builder(node: String): NodeDemoteCommand = NodeDemoteCommand(node)

        @JvmStatic
        fun builder(nodes: List<String>): NodeDemoteCommand = NodeDemoteCommand(nodes)
    }
}
