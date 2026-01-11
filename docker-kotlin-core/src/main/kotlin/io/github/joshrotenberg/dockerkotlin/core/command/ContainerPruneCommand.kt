package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Result from container prune operation.
 */
data class PruneResult(
    val deletedIds: List<String>,
    val spaceReclaimed: String
)

/**
 * Command to remove all stopped containers.
 *
 * Equivalent to `docker container prune`.
 *
 * Example usage:
 * ```kotlin
 * val result = ContainerPruneCommand().force().execute()
 * println("Deleted ${result.deletedIds.size} containers")
 * ```
 */
class ContainerPruneCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<PruneResult>(executor) {

    private val filters = mutableListOf<String>()
    private var force: Boolean = false

    /** Filter which containers to prune. */
    fun filter(filter: String) = apply { this.filters.add(filter) }

    /** Prune containers created more than given duration ago (e.g., "24h"). */
    fun filterUntil(duration: String) = filter("until=$duration")

    /** Prune containers with specific label. */
    fun filterLabel(label: String) = filter("label=$label")

    /** Do not prompt for confirmation. */
    fun force() = apply { this.force = true }

    override fun buildArgs(): List<String> = buildList {
        add("container")
        add("prune")
        filters.forEach { add("--filter"); add(it) }
        if (force) add("--force")
    }

    override suspend fun execute(): PruneResult {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): PruneResult {
        return parseOutput(executeRawBlocking().stdout)
    }

    private fun parseOutput(stdout: String): PruneResult {
        val deletedIds = mutableListOf<String>()
        var spaceReclaimed = "0B"

        stdout.lines().forEach { line ->
            when {
                line.startsWith("Deleted") || line.matches(Regex("[a-f0-9]{12,64}")) -> {
                    val id = line.replace("Deleted ", "").trim()
                    if (id.matches(Regex("[a-f0-9]{12,64}"))) {
                        deletedIds.add(id)
                    }
                }

                line.contains("Total reclaimed space:") -> {
                    spaceReclaimed = line.substringAfter(":").trim()
                }
            }
        }

        return PruneResult(deletedIds, spaceReclaimed)
    }

    companion object {
        @JvmStatic
        fun builder(): ContainerPruneCommand = ContainerPruneCommand()
    }
}
