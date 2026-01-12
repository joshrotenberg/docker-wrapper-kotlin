package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Token type for join-token command.
 */
enum class TokenType(val value: String) {
    WORKER("worker"),
    MANAGER("manager")
}

/**
 * Command to manage join tokens.
 *
 * Equivalent to `docker swarm join-token`.
 *
 * Example usage:
 * ```kotlin
 * // Get worker join token
 * val token = SwarmJoinTokenCommand(TokenType.WORKER)
 *     .quiet()
 *     .execute()
 *
 * // Rotate manager token
 * SwarmJoinTokenCommand(TokenType.MANAGER)
 *     .rotate()
 *     .execute()
 * ```
 */
class SwarmJoinTokenCommand(
    private val tokenType: TokenType,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var quiet = false
    private var rotate = false

    /** Only display token. */
    fun quiet() = apply { quiet = true }

    /** Rotate join token. */
    fun rotate() = apply { rotate = true }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("join-token")
        if (quiet) add("--quiet")
        if (rotate) add("--rotate")
        add(tokenType.value)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(tokenType: TokenType): SwarmJoinTokenCommand = SwarmJoinTokenCommand(tokenType)

        @JvmStatic
        fun worker(): SwarmJoinTokenCommand = SwarmJoinTokenCommand(TokenType.WORKER)

        @JvmStatic
        fun manager(): SwarmJoinTokenCommand = SwarmJoinTokenCommand(TokenType.MANAGER)
    }
}
