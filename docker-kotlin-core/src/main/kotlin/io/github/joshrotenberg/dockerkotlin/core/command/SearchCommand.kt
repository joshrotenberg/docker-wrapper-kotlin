package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Represents a search result from Docker Hub.
 */
data class SearchResult(
    val name: String,
    val description: String,
    val stars: Int,
    val official: Boolean,
    val automated: Boolean
)

/**
 * Command to search Docker Hub for images.
 *
 * Equivalent to `docker search`.
 *
 * Example usage:
 * ```kotlin
 * val results = SearchCommand("nginx").execute()
 * val results = SearchCommand("redis").stars(100).official().execute()
 * ```
 */
class SearchCommand(
    private val term: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<SearchResult>>(executor) {

    private val filters = mutableListOf<String>()
    private var format: String? = null
    private var limit: Int? = null
    private var noTrunc: Boolean = false

    /** Filter output based on conditions. */
    fun filter(filter: String) = apply { this.filters.add(filter) }

    /** Only show images with at least this many stars. */
    fun stars(minStars: Int) = filter("stars=$minStars")

    /** Only show official images. */
    fun official() = filter("is-official=true")

    /** Only show automated builds. */
    fun automated() = filter("is-automated=true")

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Max number of search results. */
    fun limit(limit: Int) = apply { this.limit = limit }

    /** Don't truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    override fun buildArgs(): List<String> = buildList {
        add("search")
        filters.forEach { add("--filter"); add(it) }
        format?.let { add("--format"); add(it) }
        limit?.let { add("--limit"); add(it.toString()) }
        if (noTrunc) add("--no-trunc")
        add(term)
    }

    override suspend fun execute(): List<SearchResult> {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<SearchResult> {
        return parseOutput(executeRawBlocking().stdout)
    }

    private fun parseOutput(stdout: String): List<SearchResult> {
        val lines = stdout.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            parseSearchLine(line)
        }
    }

    private fun parseSearchLine(line: String): SearchResult? {
        val parts = line.split(Regex("\\s{2,}"))
        return if (parts.size >= 5) {
            SearchResult(
                name = parts[0],
                description = parts[1],
                stars = parts[2].toIntOrNull() ?: 0,
                official = parts[3].contains("[OK]") || parts[3].lowercase() == "ok",
                automated = parts.getOrElse(4) { "" }.contains("[OK]") ||
                        parts.getOrElse(4) { "" }.lowercase() == "ok"
            )
        } else null
    }

    companion object {
        @JvmStatic
        fun builder(term: String): SearchCommand = SearchCommand(term)
    }
}
