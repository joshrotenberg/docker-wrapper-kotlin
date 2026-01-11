package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to remove a container.
 *
 * Equivalent to `docker rm`.
 *
 * Example usage:
 * ```kotlin
 * RmCommand("my-container")
 *     .force()
 *     .volumes()
 *     .execute()
 * ```
 */
class RmCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var force = false
    private var removeVolumes = false
    private var link = false

    /** Force removal of a running container. */
    fun force() = apply { force = true }

    /** Remove anonymous volumes associated with the container. */
    fun volumes() = apply { removeVolumes = true }

    /** Remove the specified link. */
    fun link() = apply { link = true }

    override fun buildArgs(): List<String> = buildList {
        add("rm")
        if (force) add("--force")
        if (removeVolumes) add("--volumes")
        if (link) add("--link")
        add(container)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): RmCommand = RmCommand(container)
    }
}
