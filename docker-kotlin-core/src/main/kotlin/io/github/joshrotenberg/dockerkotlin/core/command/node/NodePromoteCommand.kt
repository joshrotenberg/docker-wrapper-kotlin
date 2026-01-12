package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to promote one or more nodes to manager in the swarm.
 *
 * Equivalent to `docker node promote`.
 *
 * Example usage:
 * ```kotlin
 * NodePromoteCommand("node1").executeBlocking()
 * NodePromoteCommand(listOf("node1", "node2")).executeBlocking()
 * ```
 */
class NodePromoteCommand : AbstractDockerCommand<String> {

    private val nodes: List<String>

    constructor(node: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = listOf(node)
    }

    constructor(nodes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.nodes = nodes
    }

    override fun buildArgs(): List<String> = buildList {
        add("node")
        add("promote")
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
        fun builder(node: String): NodePromoteCommand = NodePromoteCommand(node)

        @JvmStatic
        fun builder(nodes: List<String>): NodePromoteCommand = NodePromoteCommand(nodes)
    }
}
