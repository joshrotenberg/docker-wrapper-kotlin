package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more services.
 *
 * Equivalent to `docker service inspect`.
 *
 * Example usage:
 * ```kotlin
 * ServiceInspectCommand("my-service").executeBlocking()
 * ServiceInspectCommand("my-service").pretty().executeBlocking()
 * ServiceInspectCommand("my-service").format("{{.ID}}").executeBlocking()
 * ```
 */
class ServiceInspectCommand : AbstractDockerCommand<String> {

    private val services: List<String>
    private var format: String? = null
    private var pretty = false

    constructor(service: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = listOf(service)
    }

    constructor(services: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = services
    }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Print the information in a human friendly format. */
    fun pretty() = apply { pretty = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (pretty) add("--pretty")
        addAll(services)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(service: String): ServiceInspectCommand = ServiceInspectCommand(service)

        @JvmStatic
        fun builder(services: List<String>): ServiceInspectCommand = ServiceInspectCommand(services)
    }
}
