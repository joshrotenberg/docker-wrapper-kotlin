package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to force stop service containers.
 *
 * Equivalent to `docker compose kill`.
 *
 * Example usage:
 * ```kotlin
 * ComposeKillCommand()
 *     .file("docker-compose.yml")
 *     .signal("SIGTERM")
 *     .services("web")
 *     .execute()
 * ```
 */
class ComposeKillCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeKillCommand>(executor) {

    private var signal: String? = null
    private var removeOrphans = false
    private val services = mutableListOf<String>()

    /** Signal to send to the container (default SIGKILL). */
    fun signal(signal: String) = apply { this.signal = signal }

    /** Remove containers for services not defined in the Compose file. */
    fun removeOrphans() = apply { removeOrphans = true }

    /** Specify services to kill. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "kill"

    override fun buildSubcommandArgs(): List<String> = buildList {
        signal?.let { add("--signal"); add(it) }
        if (removeOrphans) add("--remove-orphans")
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
        fun builder(): ComposeKillCommand = ComposeKillCommand()
    }
}
