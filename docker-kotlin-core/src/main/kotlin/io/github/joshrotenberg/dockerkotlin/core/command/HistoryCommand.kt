package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Represents an image layer from docker history output.
 */
data class ImageLayer(
    val id: String,
    val createdBy: String,
    val size: String,
    val comment: String
)

/**
 * Command to show the history of an image.
 *
 * Equivalent to `docker history`.
 *
 * Example usage:
 * ```kotlin
 * val layers = HistoryCommand("nginx:latest").execute()
 * ```
 */
class HistoryCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var format: String? = null
    private var human: Boolean = true
    private var noTrunc: Boolean = false
    private var quiet: Boolean = false

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Print sizes and dates in human readable format (default true). */
    fun human(human: Boolean) = apply { this.human = human }

    /** Don't truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    /** Only show image IDs. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("history")
        format?.let { add("--format"); add(it) }
        if (!human) add("--human=false")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
        add(image)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(image: String): HistoryCommand = HistoryCommand(image)
    }
}
