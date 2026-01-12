package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to scale one or multiple replicated services.
 *
 * Equivalent to `docker service scale`.
 *
 * Example usage:
 * ```kotlin
 * ServiceScaleCommand("my-service", 5).executeBlocking()
 * ServiceScaleCommand(mapOf("service1" to 3, "service2" to 5)).executeBlocking()
 * ```
 */
class ServiceScaleCommand : AbstractDockerCommand<String> {

    private val scales: Map<String, Int>
    private var detach = true

    constructor(service: String, replicas: Int, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.scales = mapOf(service to replicas)
    }

    constructor(scales: Map<String, Int>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.scales = scales
    }

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("scale")
        if (detach) add("--detach")
        scales.forEach { (service, replicas) -> add("$service=$replicas") }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(service: String, replicas: Int): ServiceScaleCommand =
            ServiceScaleCommand(service, replicas)

        @JvmStatic
        fun builder(scales: Map<String, Int>): ServiceScaleCommand =
            ServiceScaleCommand(scales)
    }
}
