package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list services.
 *
 * Equivalent to `docker service ls`.
 *
 * Example usage:
 * ```kotlin
 * ServiceLsCommand().executeBlocking()
 * ServiceLsCommand().filter("name", "my-service").executeBlocking()
 * ServiceLsCommand().quiet().executeBlocking()
 * ```
 */
class ServiceLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var quiet = false

    /** Filter output based on conditions provided. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Only display service IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("ls")
        filters.forEach { (key, value) -> add("--filter"); add("$key=$value") }
        format?.let { add("--format"); add(it) }
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): ServiceLsCommand = ServiceLsCommand()
    }
}
