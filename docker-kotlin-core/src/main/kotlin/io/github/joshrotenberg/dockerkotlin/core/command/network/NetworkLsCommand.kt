package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.model.NetworkSummary
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to list Docker networks.
 *
 * Equivalent to `docker network ls`.
 *
 * Example usage:
 * ```kotlin
 * val networks = NetworkLsCommand().execute()
 * val bridgeNetworks = NetworkLsCommand().driverFilter("bridge").execute()
 * ```
 */
class NetworkLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<NetworkSummary>>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var customFormat: String? = null
    private var noTrunc = false
    private var quiet = false

    /** Add a filter. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Filter by driver. */
    fun driverFilter(driver: String) = apply { filter("driver", driver) }

    /** Filter by ID. */
    fun idFilter(id: String) = apply { filter("id", id) }

    /** Filter by label. */
    fun labelFilter(label: String) = apply { filter("label", label) }

    /** Filter by name. */
    fun nameFilter(name: String) = apply { filter("name", name) }

    /** Filter by scope. */
    fun scopeFilter(scope: String) = apply { filter("scope", scope) }

    /** Filter by type (custom or builtin). */
    fun typeFilter(type: String) = apply { filter("type", type) }

    /** Set output format (overrides JSON output). */
    fun format(format: String) = apply { this.customFormat = format }

    /** Don't truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Only display network IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
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
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): List<NetworkSummary> {
        return parseJsonOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<NetworkSummary> {
        return parseJsonOutput(executeRawBlocking().stdout)
    }

    /**
     * Execute and return only network IDs.
     */
    suspend fun executeIds(): List<String> {
        val originalQuiet = quiet
        quiet = true
        val output = executeRaw()
        quiet = originalQuiet
        return output.stdout.lines().filter { it.isNotBlank() }.map { it.trim() }
    }

    /**
     * Execute and return only network IDs (blocking).
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

    private fun parseJsonOutput(stdout: String): List<NetworkSummary> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        return lines.mapNotNull { line ->
            runCatching {
                json.decodeFromString<NetworkSummary>(line)
            }.getOrNull()
        }
    }

    companion object {
        @JvmStatic
        fun builder(): NetworkLsCommand = NetworkLsCommand()
    }
}
