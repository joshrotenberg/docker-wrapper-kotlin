package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to unlock a locked swarm manager.
 *
 * Equivalent to `docker swarm unlock`.
 *
 * Note: This command reads the unlock key from stdin when run interactively.
 * For programmatic use, you may need to pipe the key or use other mechanisms.
 *
 * Example usage:
 * ```kotlin
 * SwarmUnlockCommand()
 *     .execute()
 * ```
 */
class SwarmUnlockCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    override fun buildArgs(): List<String> = listOf("swarm", "unlock")

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmUnlockCommand = SwarmUnlockCommand()
    }
}
