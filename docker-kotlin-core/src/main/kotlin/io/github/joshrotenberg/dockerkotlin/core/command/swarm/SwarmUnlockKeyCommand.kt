package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to manage the unlock key.
 *
 * Equivalent to `docker swarm unlock-key`.
 *
 * Example usage:
 * ```kotlin
 * // Get current unlock key
 * val key = SwarmUnlockKeyCommand()
 *     .quiet()
 *     .execute()
 *
 * // Rotate the unlock key
 * SwarmUnlockKeyCommand()
 *     .rotate()
 *     .execute()
 * ```
 */
class SwarmUnlockKeyCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var quiet = false
    private var rotate = false

    /** Only display the unlock key. */
    fun quiet() = apply { quiet = true }

    /** Rotate unlock key. */
    fun rotate() = apply { rotate = true }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("unlock-key")
        if (quiet) add("--quiet")
        if (rotate) add("--rotate")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmUnlockKeyCommand = SwarmUnlockKeyCommand()
    }
}
