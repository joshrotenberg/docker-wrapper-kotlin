package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to start existing containers.
 *
 * Equivalent to `docker compose start`.
 *
 * Example usage:
 * ```kotlin
 * ComposeStartCommand()
 *     .file("docker-compose.yml")
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposeStartCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeStartCommand>(executor) {

    private val services = mutableListOf<String>()

    /** Specify services to start. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "start"

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
        fun builder(): ComposeStartCommand = ComposeStartCommand()
    }
}
