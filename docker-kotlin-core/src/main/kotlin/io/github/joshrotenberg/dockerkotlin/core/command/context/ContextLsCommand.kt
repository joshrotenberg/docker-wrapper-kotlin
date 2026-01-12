package io.github.joshrotenberg.dockerkotlin.core.command.context

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Information about a Docker context.
 */
data class ContextInfo(
    val name: String,
    val description: String,
    val dockerEndpoint: String,
    val current: Boolean
)

/**
 * Command to list Docker contexts.
 *
 * Equivalent to `docker context ls`.
 *
 * Example usage:
 * ```kotlin
 * val contexts = ContextLsCommand()
 *     .execute()
 *
 * contexts.forEach { println("${it.name}: ${it.dockerEndpoint}") }
 * ```
 */
class ContextLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<ContextInfo>>(executor) {

    private var format: String? = null
    private var quiet = false

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Only show context names. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("context")
        add("ls")
        format?.let { add("--format"); add(it) }
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): List<ContextInfo> {
        val output = executeRaw()
        return parseOutput(output.stdout)
    }

    override fun executeBlocking(): List<ContextInfo> {
        val output = executeRawBlocking()
        return parseOutput(output.stdout)
    }

    private fun parseOutput(output: String): List<ContextInfo> {
        val lines = output.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        // Skip header line and parse data
        return lines.drop(1).mapNotNull { line ->
            // Format: NAME   DESCRIPTION   DOCKER ENDPOINT   ERROR
            // Current context is marked with *
            val current = line.trimStart().startsWith("*")
            val cleanLine = line.replace("*", " ")
            val parts = cleanLine.split("\\s{2,}".toRegex()).map { it.trim() }
            if (parts.isNotEmpty()) {
                ContextInfo(
                    name = parts[0],
                    description = parts.getOrElse(1) { "" },
                    dockerEndpoint = parts.getOrElse(2) { "" },
                    current = current
                )
            } else null
        }
    }

    companion object {
        @JvmStatic
        fun builder(): ContextLsCommand = ContextLsCommand()
    }
}
