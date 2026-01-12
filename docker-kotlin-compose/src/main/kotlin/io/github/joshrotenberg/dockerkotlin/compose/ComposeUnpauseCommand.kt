package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to unpause paused containers.
 *
 * Equivalent to `docker compose unpause`.
 *
 * Example usage:
 * ```kotlin
 * ComposeUnpauseCommand()
 *     .file("docker-compose.yml")
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposeUnpauseCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeUnpauseCommand>(executor) {

    private val services = mutableListOf<String>()

    /** Specify services to unpause. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "unpause"

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
        fun builder(): ComposeUnpauseCommand = ComposeUnpauseCommand()
    }
}
