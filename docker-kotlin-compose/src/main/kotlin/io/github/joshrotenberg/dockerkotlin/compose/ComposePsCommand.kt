package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Information about a compose service container.
 */
data class ComposeServiceInfo(
    val name: String,
    val service: String,
    val status: String,
    val ports: String
)

/**
 * Command to list compose services.
 *
 * Equivalent to `docker compose ps`.
 *
 * Example usage:
 * ```kotlin
 * val services = ComposePsCommand()
 *     .file("docker-compose.yml")
 *     .execute()
 *
 * services.forEach { println("${it.name}: ${it.status}") }
 * ```
 */
class ComposePsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<List<ComposeServiceInfo>>(executor) {

    private var all = false
    private var quiet = false
    private var format: String? = null
    private val services = mutableListOf<String>()
    private var status: ServiceStatus? = null

    /** Show all containers (including stopped). */
    fun all() = apply { all = true }

    /** Only display container IDs. */
    fun quiet() = apply { quiet = true }

    /** Format output. */
    fun format(format: String) = apply { this.format = format }

    /** Filter by service status. */
    fun status(status: ServiceStatus) = apply { this.status = status }

    /** Specify services to list. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "ps"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (all) add("--all")
        if (quiet) add("--quiet")
        format?.let { add("--format"); add(it) }
        status?.let { add("--status"); add(it.value) }
        addAll(services)
    }

    override suspend fun execute(): List<ComposeServiceInfo> {
        val output = executeRaw()
        return parseOutput(output.stdout)
    }

    override fun executeBlocking(): List<ComposeServiceInfo> {
        val output = executeRawBlocking()
        return parseOutput(output.stdout)
    }

    private fun parseOutput(output: String): List<ComposeServiceInfo> {
        val lines = output.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        // Skip header line and parse data
        return lines.drop(1).mapNotNull { line ->
            val parts = line.split("\\s{2,}".toRegex())
            if (parts.size >= 3) {
                ComposeServiceInfo(
                    name = parts[0],
                    service = parts.getOrElse(1) { "" },
                    status = parts.getOrElse(2) { "" },
                    ports = parts.getOrElse(3) { "" }
                )
            } else null
        }
    }

    companion object {
        @JvmStatic
        fun builder(): ComposePsCommand = ComposePsCommand()
    }
}

/**
 * Service status filter.
 */
enum class ServiceStatus(val value: String) {
    PAUSED("paused"),
    RESTARTING("restarting"),
    REMOVING("removing"),
    RUNNING("running"),
    DEAD("dead"),
    CREATED("created"),
    EXITED("exited")
}
