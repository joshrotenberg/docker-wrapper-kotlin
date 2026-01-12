package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.model.ImageSummary
import kotlinx.serialization.json.Json

/**
 * Represents an image from docker images output.
 *
 * @deprecated Use [ImageSummary] instead for typed JSON responses.
 */
@Deprecated("Use ImageSummary instead", ReplaceWith("ImageSummary"))
data class ImageInfo(
    val repository: String,
    val tag: String,
    val imageId: String,
    val created: String,
    val size: String
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to list images.
 *
 * Equivalent to `docker images`.
 *
 * Example usage:
 * ```kotlin
 * val images = ImagesCommand().execute()
 * val nginxImages = ImagesCommand("nginx").execute()
 * ```
 */
class ImagesCommand(
    private val repository: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<ImageSummary>>(executor) {

    private var all: Boolean = false
    private var digests: Boolean = false
    private val filters = mutableListOf<String>()
    private var customFormat: String? = null
    private var noTrunc: Boolean = false
    private var quiet: Boolean = false

    /** Show all images (default hides intermediate images). */
    fun all() = apply { this.all = true }

    /** Show digests. */
    fun digests() = apply { this.digests = true }

    /** Filter output based on conditions. */
    fun filter(filter: String) = apply { this.filters.add(filter) }

    /** Filter by dangling. */
    fun filterDangling(dangling: Boolean = true) = filter("dangling=$dangling")

    /** Filter by label. */
    fun filterLabel(label: String) = filter("label=$label")

    /** Filter by reference. */
    fun filterReference(reference: String) = filter("reference=$reference")

    /** Filter images created before given image. */
    fun filterBefore(image: String) = filter("before=$image")

    /** Filter images created since given image. */
    fun filterSince(image: String) = filter("since=$image")

    /** Format output using a Go template (overrides JSON output). */
    fun format(format: String) = apply { this.customFormat = format }

    /** Don't truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    /** Only show image IDs. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("images")
        if (all) add("--all")
        if (digests) add("--digests")
        filters.forEach { add("--filter"); add(it) }
        // Use JSON format unless custom format or quiet is specified
        if (customFormat != null) {
            add("--format"); add(customFormat!!)
        } else if (!quiet) {
            add("--format"); add("json")
        }
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        repository?.let { add(it) }
    }

    override suspend fun execute(): List<ImageSummary> {
        return parseJsonOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<ImageSummary> {
        return parseJsonOutput(executeRawBlocking().stdout)
    }

    /**
     * Execute and return only image IDs.
     */
    suspend fun executeIds(): List<String> {
        val originalQuiet = quiet
        quiet = true
        val output = executeRaw()
        quiet = originalQuiet
        return output.stdout.lines().filter { it.isNotBlank() }.map { it.trim() }
    }

    /**
     * Execute and return only image IDs (blocking).
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

    private fun parseJsonOutput(stdout: String): List<ImageSummary> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        // Each line is a separate JSON object (NDJSON format)
        return lines.mapNotNull { line ->
            runCatching {
                json.decodeFromString<ImageSummary>(line)
            }.getOrNull()
        }
    }

    companion object {
        @JvmStatic
        fun builder(): ImagesCommand = ImagesCommand()

        @JvmStatic
        fun builder(repository: String): ImagesCommand = ImagesCommand(repository)
    }
}
