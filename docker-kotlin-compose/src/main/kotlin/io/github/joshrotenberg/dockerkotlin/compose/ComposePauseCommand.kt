package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to pause running containers.
 *
 * Equivalent to `docker compose pause`.
 *
 * Example usage:
 * ```kotlin
 * ComposePauseCommand()
 *     .file("docker-compose.yml")
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposePauseCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposePauseCommand>(executor) {

    private val services = mutableListOf<String>()

    /** Specify services to pause. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "pause"

    override fun buildSubcommandArgs(): List<String> = buildList {
        addAll(services)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): ComposePauseCommand = ComposePauseCommand()
    }
}
