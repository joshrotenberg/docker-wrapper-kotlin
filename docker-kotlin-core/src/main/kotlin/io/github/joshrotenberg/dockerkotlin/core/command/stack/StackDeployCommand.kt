package io.github.joshrotenberg.dockerkotlin.core.command.stack

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to deploy a new stack or update an existing stack.
 *
 * Equivalent to `docker stack deploy`.
 *
 * Example usage:
 * ```kotlin
 * StackDeployCommand("my-stack", "docker-compose.yml").executeBlocking()
 * StackDeployCommand("my-stack", listOf("docker-compose.yml", "docker-compose.prod.yml"))
 *     .withRegistryAuth()
 *     .executeBlocking()
 * ```
 */
class StackDeployCommand : AbstractDockerCommand<String> {

    private val stack: String
    private val composeFiles: List<String>
    private var prune = false
    private var resolveImage: String? = null
    private var withRegistryAuth = false
    private var detach = true
    private var quiet = false

    constructor(
        stack: String,
        composeFile: String,
        executor: CommandExecutor = CommandExecutor()
    ) : super(executor) {
        this.stack = stack
        this.composeFiles = listOf(composeFile)
    }

    constructor(
        stack: String,
        composeFiles: List<String>,
        executor: CommandExecutor = CommandExecutor()
    ) : super(executor) {
        this.stack = stack
        this.composeFiles = composeFiles
    }

    /** Prune services that are no longer referenced. */
    fun prune() = apply { prune = true }

    /** Query the registry to resolve image digest and supported platforms (always, changed, never). */
    fun resolveImage(mode: String) = apply { resolveImage = mode }

    /** Send registry authentication details to Swarm agents. */
    fun withRegistryAuth() = apply { withRegistryAuth = true }

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    /** Suppress progress output. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("stack")
        add("deploy")
        composeFiles.forEach { add("--compose-file"); add(it) }
        if (prune) add("--prune")
        resolveImage?.let { add("--resolve-image"); add(it) }
        if (withRegistryAuth) add("--with-registry-auth")
        if (detach) add("--detach")
        if (quiet) add("--quiet")
        add(stack)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(stack: String, composeFile: String): StackDeployCommand =
            StackDeployCommand(stack, composeFile)

        @JvmStatic
        fun builder(stack: String, composeFiles: List<String>): StackDeployCommand =
            StackDeployCommand(stack, composeFiles)
    }
}
