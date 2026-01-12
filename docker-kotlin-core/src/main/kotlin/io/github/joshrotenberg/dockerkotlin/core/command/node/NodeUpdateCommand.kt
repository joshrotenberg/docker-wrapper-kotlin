package io.github.joshrotenberg.dockerkotlin.core.command.node

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to update a node.
 *
 * Equivalent to `docker node update`.
 *
 * Example usage:
 * ```kotlin
 * NodeUpdateCommand("node1").availability("drain").executeBlocking()
 * NodeUpdateCommand("node1").labelAdd("env", "prod").executeBlocking()
 * NodeUpdateCommand("node1").role("manager").executeBlocking()
 * ```
 */
class NodeUpdateCommand(
    private val node: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var availability: String? = null
    private val labelAdd = mutableMapOf<String, String>()
    private val labelRm = mutableListOf<String>()
    private var role: String? = null

    /** Node availability (active, pause, drain). */
    fun availability(availability: String) = apply { this.availability = availability }

    /** Add a node label. */
    fun labelAdd(key: String, value: String) = apply { labelAdd[key] = value }

    /** Remove a node label. */
    fun labelRm(key: String) = apply { labelRm.add(key) }

    /** Node role (worker, manager). */
    fun role(role: String) = apply { this.role = role }

    override fun buildArgs(): List<String> = buildList {
        add("node")
        add("update")
        availability?.let { add("--availability"); add(it) }
        labelAdd.forEach { (key, value) -> add("--label-add"); add("$key=$value") }
        labelRm.forEach { add("--label-rm"); add(it) }
        role?.let { add("--role"); add(it) }
        add(node)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(node: String): NodeUpdateCommand = NodeUpdateCommand(node)
    }
}
