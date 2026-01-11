package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Represents an image from docker images output.
 */
data class ImageInfo(
    val repository: String,
    val tag: String,
    val imageId: String,
    val created: String,
    val size: String
)

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
) : AbstractDockerCommand<List<ImageInfo>>(executor) {

    private var all: Boolean = false
    private var digests: Boolean = false
    private val filters = mutableListOf<String>()
    private var format: String? = null
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

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Don't truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    /** Only show image IDs. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("images")
        if (all) add("--all")
        if (digests) add("--digests")
        filters.forEach { add("--filter"); add(it) }
        format?.let { add("--format"); add(it) }
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        repository?.let { add(it) }
    }

    override suspend fun execute(): List<ImageInfo> {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<ImageInfo> {
        return parseOutput(executeRawBlocking().stdout)
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

    private fun parseOutput(stdout: String): List<ImageInfo> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(Regex("\\s{2,}"))
            if (parts.size >= 5) {
                ImageInfo(
                    repository = parts[0],
                    tag = parts[1],
                    imageId = parts[2],
                    created = parts[3],
                    size = parts[4]
                )
            } else null
        }
    }

    companion object {
        @JvmStatic
        fun builder(): ImagesCommand = ImagesCommand()

        @JvmStatic
        fun builder(repository: String): ImagesCommand = ImagesCommand(repository)
    }
}
