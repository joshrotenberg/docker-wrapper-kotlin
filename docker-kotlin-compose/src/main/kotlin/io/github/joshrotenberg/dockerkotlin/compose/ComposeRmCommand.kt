package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to remove stopped service containers.
 *
 * Equivalent to `docker compose rm`.
 *
 * Example usage:
 * ```kotlin
 * ComposeRmCommand()
 *     .file("docker-compose.yml")
 *     .force()
 *     .volumes()
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposeRmCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeRmCommand>(executor) {

    private var force = false
    private var stop = false
    private var volumes = false
    private val services = mutableListOf<String>()

    /** Don't ask to confirm removal. */
    fun force() = apply { force = true }

    /** Stop the containers, if required, before removing. */
    fun stop() = apply { stop = true }

    /** Remove any anonymous volumes attached to containers. */
    fun volumes() = apply { volumes = true }

    /** Specify services to remove. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "rm"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (force) add("--force")
        if (stop) add("--stop")
        if (volumes) add("--volumes")
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
        fun builder(): ComposeRmCommand = ComposeRmCommand()
    }
}
