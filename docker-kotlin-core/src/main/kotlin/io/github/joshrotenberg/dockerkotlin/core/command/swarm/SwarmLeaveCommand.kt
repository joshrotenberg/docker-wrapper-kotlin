package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to leave the swarm.
 *
 * Equivalent to `docker swarm leave`.
 *
 * Example usage:
 * ```kotlin
 * SwarmLeaveCommand()
 *     .force()
 *     .execute()
 * ```
 */
class SwarmLeaveCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var force = false

    /** Force this node to leave the swarm, ignoring warnings. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("leave")
        if (force) add("--force")
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmLeaveCommand = SwarmLeaveCommand()
    }
}
