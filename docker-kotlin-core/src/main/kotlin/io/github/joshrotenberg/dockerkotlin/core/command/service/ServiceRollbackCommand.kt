package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to revert changes to a service's configuration.
 *
 * Equivalent to `docker service rollback`.
 *
 * Example usage:
 * ```kotlin
 * ServiceRollbackCommand("my-service").executeBlocking()
 * ServiceRollbackCommand("my-service").quiet().executeBlocking()
 * ```
 */
class ServiceRollbackCommand(
    private val service: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var detach = true
    private var quiet = false

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    /** Suppress progress output. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("rollback")
        if (detach) add("--detach")
        if (quiet) add("--quiet")
        add(service)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(service: String): ServiceRollbackCommand = ServiceRollbackCommand(service)
    }
}
