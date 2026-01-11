package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Type of filesystem change.
 */
enum class ChangeType(val symbol: String) {
    ADDED("A"),
    DELETED("D"),
    CHANGED("C");

    companion object {
        fun fromSymbol(symbol: String): ChangeType? =
            entries.find { it.symbol == symbol }
    }
}

/**
 * Represents a filesystem change in a container.
 */
data class FilesystemChange(
    val type: ChangeType,
    val path: String
)

/**
 * Command to inspect changes to files or directories on a container's filesystem.
 *
 * Equivalent to `docker diff`.
 *
 * Example usage:
 * ```kotlin
 * val changes = DiffCommand("my-container").execute()
 * ```
 */
class DiffCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<FilesystemChange>>(executor) {

    override fun buildArgs(): List<String> = buildList {
        add("diff")
        add(container)
    }

    override suspend fun execute(): List<FilesystemChange> {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<FilesystemChange> {
        return parseOutput(executeRawBlocking().stdout)
    }

    private fun parseOutput(stdout: String): List<FilesystemChange> {
        return stdout.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(" ", limit = 2)
                if (parts.size == 2) {
                    val type = ChangeType.fromSymbol(parts[0])
                    if (type != null) {
                        FilesystemChange(type, parts[1])
                    } else null
                } else null
            }
    }

    companion object {
        @JvmStatic
        fun builder(container: String): DiffCommand = DiffCommand(container)
    }
}
