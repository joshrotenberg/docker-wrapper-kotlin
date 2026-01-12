package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.model.ContainerSummary
import kotlinx.serialization.json.Json

/**
 * Represents a container from docker ps output.
 *
 * @deprecated Use [ContainerSummary] instead for typed JSON responses.
 */
@Deprecated("Use ContainerSummary instead", ReplaceWith("ContainerSummary"))
data class ContainerInfo(
    val id: String,
    val image: String,
    val command: String,
    val created: String,
    val status: String,
    val ports: String,
    val names: String
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to list containers.
 *
 * Equivalent to `docker ps`.
 *
 * Example usage:
 * ```kotlin
 * val containers = PsCommand().execute()
 * val allContainers = PsCommand().all().execute()
 * ```
 */
class PsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<ContainerSummary>>(executor) {

    private var all: Boolean = false
    private val filters = mutableListOf<String>()
    private var customFormat: String? = null
    private var last: Int? = null
    private var latest: Boolean = false
    private var noTrunc: Boolean = false
    private var quiet: Boolean = false
    private var showSize: Boolean = false

    /** Show all containers (default shows just running). */
    fun all() = apply { this.all = true }

    /** Filter output based on conditions. */
    fun filter(filter: String) = apply { this.filters.add(filter) }

    /** Filter by name. */
    fun filterName(name: String) = filter("name=$name")

    /** Filter by status (created, restarting, running, removing, paused, exited, dead). */
    fun filterStatus(status: String) = filter("status=$status")

    /** Filter by ancestor image. */
    fun filterAncestor(image: String) = filter("ancestor=$image")

    /** Filter by label. */
    fun filterLabel(label: String) = filter("label=$label")

    /** Pretty-print containers using a Go template (overrides JSON output). */
    fun format(format: String) = apply { this.customFormat = format }

    /** Show n last created containers (includes all states). */
    fun last(n: Int) = apply { this.last = n }

    /** Show the latest created container (includes all states). */
    fun latest() = apply { this.latest = true }

    /** Don't truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    /** Only display container IDs. */
    fun quiet() = apply { this.quiet = true }

    /** Display total file sizes. */
    fun size() = apply { this.showSize = true }

    override fun buildArgs(): List<String> = buildList {
        add("ps")
        if (all) add("--all")
        filters.forEach { add("--filter"); add(it) }
        // Use JSON format unless custom format or quiet is specified
        if (customFormat != null) {
            add("--format"); add(customFormat!!)
        } else if (!quiet) {
            add("--format"); add("json")
        }
        last?.let { add("--last"); add(it.toString()) }
        if (latest) add("--latest")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        if (showSize) add("--size")
    }

    override suspend fun execute(): List<ContainerSummary> {
        return parseJsonOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<ContainerSummary> {
        return parseJsonOutput(executeRawBlocking().stdout)
    }

    /**
     * Execute and return only container IDs.
     */
    suspend fun executeIds(): List<String> {
        val originalQuiet = quiet
        quiet = true
        val output = executeRaw()
        quiet = originalQuiet
        return output.stdout.lines().filter { it.isNotBlank() }.map { it.trim() }
    }

    /**
     * Execute and return only container IDs (blocking).
     */
    fun executeIdsBlocking(): List<String> {
        val originalQuiet = quiet
        quiet = true
        val output = executeRawBlocking()
        quiet = originalQuiet
        return output.stdout.lines().filter { it.isNotBlank() }.map { it.trim() }
    }

    /**
     * Execute with custom format and return raw string output.
     */
    suspend fun executeRawFormat(format: String): String {
        val originalFormat = customFormat
        customFormat = format
        val output = executeRaw()
        customFormat = originalFormat
        return output.stdout
    }

    /**
     * Execute with custom format and return raw string output (blocking).
     */
    fun executeRawFormatBlocking(format: String): String {
        val originalFormat = customFormat
        customFormat = format
        val output = executeRawBlocking()
        customFormat = originalFormat
        return output.stdout
    }

    private fun parseJsonOutput(stdout: String): List<ContainerSummary> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        // Each line is a separate JSON object (NDJSON format)
        return lines.mapNotNull { line ->
            runCatching {
                json.decodeFromString<ContainerSummary>(line)
            }.getOrNull()
        }
    }

    companion object {
        @JvmStatic
        fun builder(): PsCommand = PsCommand()
    }
}
