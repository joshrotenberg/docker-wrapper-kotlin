package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list the tasks of one or more services.
 *
 * Equivalent to `docker service ps`.
 *
 * Example usage:
 * ```kotlin
 * ServicePsCommand("my-service").executeBlocking()
 * ServicePsCommand("my-service").filter("desired-state", "running").executeBlocking()
 * ServicePsCommand(listOf("service1", "service2")).noTrunc().executeBlocking()
 * ```
 */
class ServicePsCommand : AbstractDockerCommand<String> {

    private val services: List<String>
    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var noResolve = false
    private var noTrunc = false
    private var quiet = false

    constructor(service: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = listOf(service)
    }

    constructor(services: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = services
    }

    /** Filter output based on conditions provided. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Do not map IDs to names. */
    fun noResolve() = apply { noResolve = true }

    /** Do not truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Only display task IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("ps")
        filters.forEach { (key, value) -> add("--filter"); add("$key=$value") }
        format?.let { add("--format"); add(it) }
        if (noResolve) add("--no-resolve")
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
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
        fun builder(service: String): ServicePsCommand = ServicePsCommand(service)

        @JvmStatic
        fun builder(services: List<String>): ServicePsCommand = ServicePsCommand(services)
    }
}
