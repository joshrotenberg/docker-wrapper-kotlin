package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more services.
 *
 * Equivalent to `docker service rm`.
 *
 * Example usage:
 * ```kotlin
 * ServiceRmCommand("my-service").executeBlocking()
 * ServiceRmCommand(listOf("service1", "service2")).executeBlocking()
 * ```
 */
class ServiceRmCommand : AbstractDockerCommand<String> {

    private val services: List<String>

    constructor(service: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = listOf(service)
    }

    constructor(services: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.services = services
    }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("rm")
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
        fun builder(service: String): ServiceRmCommand = ServiceRmCommand(service)

        @JvmStatic
        fun builder(services: List<String>): ServiceRmCommand = ServiceRmCommand(services)
    }
}
