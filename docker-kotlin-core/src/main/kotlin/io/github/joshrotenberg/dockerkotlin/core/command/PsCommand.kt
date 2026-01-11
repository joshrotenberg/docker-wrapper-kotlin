package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Represents a container from docker ps output.
 */
data class ContainerInfo(
    val id: String,
    val image: String,
    val command: String,
    val created: String,
    val status: String,
    val ports: String,
    val names: String
)

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
) : AbstractDockerCommand<List<ContainerInfo>>(executor) {

    private var all: Boolean = false
    private val filters = mutableListOf<String>()
    private var format: String? = null
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

    /** Pretty-print containers using a Go template. */
    fun format(format: String) = apply { this.format = format }

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
        format?.let { add("--format"); add(it) }
        last?.let { add("--last"); add(it.toString()) }
        if (latest) add("--latest")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        if (showSize) add("--size")
    }

    override suspend fun execute(): List<ContainerInfo> {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<ContainerInfo> {
        return parseOutput(executeRawBlocking().stdout)
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

    private fun parseOutput(stdout: String): List<ContainerInfo> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        // Skip header line and parse containers
        return lines.drop(1).mapNotNull { line ->
            parseContainerLine(line)
        }
    }

    private fun parseContainerLine(line: String): ContainerInfo? {
        // This is a simplified parser - docker ps output is space-aligned
        // For proper parsing, use --format with JSON
        val parts = line.split(Regex("\\s{2,}"))
        return if (parts.size >= 7) {
            ContainerInfo(
                id = parts[0],
                image = parts[1],
                command = parts[2],
                created = parts[3],
                status = parts[4],
                ports = parts.getOrElse(5) { "" },
                names = parts.getOrElse(6) { "" }
            )
        } else null
    }

    companion object {
        @JvmStatic
        fun builder(): PsCommand = PsCommand()
    }
}
