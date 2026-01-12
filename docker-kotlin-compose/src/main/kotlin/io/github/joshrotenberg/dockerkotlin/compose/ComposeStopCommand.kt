package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to stop running containers.
 *
 * Equivalent to `docker compose stop`.
 *
 * Example usage:
 * ```kotlin
 * ComposeStopCommand()
 *     .file("docker-compose.yml")
 *     .timeout(30)
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposeStopCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeStopCommand>(executor) {

    private var timeout: Int? = null
    private val services = mutableListOf<String>()

    /** Specify a shutdown timeout in seconds. */
    fun timeout(seconds: Int) = apply { timeout = seconds }

    /** Specify services to stop. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "stop"

    override fun buildSubcommandArgs(): List<String> = buildList {
        timeout?.let { add("--timeout"); add(it.toString()) }
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
        fun builder(): ComposeStopCommand = ComposeStopCommand()
    }
}
