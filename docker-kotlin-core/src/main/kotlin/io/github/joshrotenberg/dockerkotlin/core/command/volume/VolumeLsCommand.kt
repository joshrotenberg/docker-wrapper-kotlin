package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.model.VolumeSummary
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to list Docker volumes.
 *
 * Equivalent to `docker volume ls`.
 *
 * Example usage:
 * ```kotlin
 * val volumes = VolumeLsCommand().execute()
 * val localVolumes = VolumeLsCommand().driverFilter("local").execute()
 * ```
 */
class VolumeLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<VolumeSummary>>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var customFormat: String? = null
    private var quiet = false

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by driver. */
    fun driverFilter(driver: String) = apply { filter("driver", driver) }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Filter by name. */
    fun nameFilter(name: String) = apply { filter("name", name) }

    /** Filter dangling volumes. */
    fun danglingFilter(dangling: Boolean = true) = apply { filter("dangling", dangling.toString()) }

    /** Set output format (overrides JSON output). */
    fun format(format: String) = apply { this.customFormat = format }

    /** Only display volume names. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("ls")

        filters.forEach { (key, value) ->
            add("--filter")
            add("$key=$value")
        }

        // Use JSON format unless custom format or quiet is specified
        if (customFormat != null) {
            add("--format"); add(customFormat!!)
        } else if (!quiet) {
            add("--format"); add("json")
        }
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): List<VolumeSummary> {
        return parseJsonOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<VolumeSummary> {
        return parseJsonOutput(executeRawBlocking().stdout)
    }

    /**
     * Execute and return only volume names.
     */
    suspend fun executeNames(): List<String> {
        val originalQuiet = quiet
        quiet = true
        val output = executeRaw()
        quiet = originalQuiet
        return output.stdout.lines().filter { it.isNotBlank() }.map { it.trim() }
    }

    /**
     * Execute and return only volume names (blocking).
     */
    fun executeNamesBlocking(): List<String> {
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

    private fun parseJsonOutput(stdout: String): List<VolumeSummary> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        return lines.mapNotNull { line ->
            runCatching {
                json.decodeFromString<VolumeSummary>(line)
            }.getOrNull()
        }
    }

    companion object {
        @JvmStatic
        fun builder(): VolumeLsCommand = VolumeLsCommand()
    }
}
