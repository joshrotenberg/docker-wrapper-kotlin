package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to restart service containers.
 *
 * Equivalent to `docker compose restart`.
 *
 * Example usage:
 * ```kotlin
 * ComposeRestartCommand()
 *     .file("docker-compose.yml")
 *     .timeout(30)
 *     .services("web", "api")
 *     .execute()
 * ```
 */
class ComposeRestartCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeRestartCommand>(executor) {

    private var noDeps = false
    private var timeout: Int? = null
    private val services = mutableListOf<String>()

    /** Don't restart dependent services. */
    fun noDeps() = apply { noDeps = true }

    /** Specify a shutdown timeout in seconds. */
    fun timeout(seconds: Int) = apply { timeout = seconds }

    /** Specify services to restart. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "restart"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (noDeps) add("--no-deps")
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
        fun builder(): ComposeRestartCommand = ComposeRestartCommand()
    }
}
