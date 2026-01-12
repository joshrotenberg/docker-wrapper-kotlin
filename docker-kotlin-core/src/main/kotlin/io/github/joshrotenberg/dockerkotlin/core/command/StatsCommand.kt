package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to display a live stream of container(s) resource usage statistics.
 *
 * Equivalent to `docker stats`.
 *
 * Example usage:
 * ```kotlin
 * // Get a single snapshot
 * val stats = StatsCommand("my-container").noStream().execute()
 * val stats = StatsCommand().all().noStream().execute()
 *
 * // Stream live stats (Kotlin)
 * StatsCommand("my-container").asFlow().collect { stats ->
 *     println(stats)
 * }
 *
 * // Stream live stats (Java)
 * try (StreamHandle handle = StatsCommand.builder("my-container").stream()) {
 *     for (String stats : handle) {
 *         System.out.println(stats);
 *     }
 * }
 * ```
 */
class StatsCommand(
    private val containers: List<String> = emptyList(),
    executor: CommandExecutor = CommandExecutor()
) : AbstractStreamingDockerCommand<String>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    private var all: Boolean = false
    private var format: String? = null
    private var noStream: Boolean = false
    private var noTrunc: Boolean = false

    /** Show all containers (default shows just running). */
    fun all() = apply { this.all = true }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Disable streaming stats and only pull the first result. */
    fun noStream() = apply { this.noStream = true }

    /** Do not truncate output. */
    fun noTrunc() = apply { this.noTrunc = true }

    override fun buildArgs(): List<String> = buildList {
        add("stats")
        if (all) add("--all")
        format?.let { add("--format"); add(it) }
        if (noStream) add("--no-stream")
        if (noTrunc) add("--no-trunc")
        addAll(containers)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): StatsCommand = StatsCommand()

        @JvmStatic
        fun builder(container: String): StatsCommand = StatsCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): StatsCommand = StatsCommand(containers)
    }
}
