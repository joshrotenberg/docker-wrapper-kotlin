package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to stop and remove compose services.
 *
 * Equivalent to `docker compose down`.
 *
 * Example usage:
 * ```kotlin
 * ComposeDownCommand()
 *     .file("docker-compose.yml")
 *     .volumes()
 *     .removeOrphans()
 *     .execute()
 * ```
 */
class ComposeDownCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeDownCommand>(executor) {

    private var removeOrphans = false
    private var removeVolumes = false
    private var removeImages: RemoveImages? = null
    private var timeout: Int? = null

    /** Remove containers for services not defined in the Compose file. */
    fun removeOrphans() = apply { removeOrphans = true }

    /** Remove named volumes. */
    fun volumes() = apply { removeVolumes = true }

    /** Remove images (all or local only). */
    fun rmi(type: RemoveImages) = apply { removeImages = type }

    /** Shutdown timeout in seconds. */
    fun timeout(seconds: Int) = apply { timeout = seconds }

    override fun subcommand(): String = "down"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (removeOrphans) add("--remove-orphans")
        if (removeVolumes) add("--volumes")
        removeImages?.let { add("--rmi"); add(it.value) }
        timeout?.let { add("--timeout"); add(it.toString()) }
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): ComposeDownCommand = ComposeDownCommand()
    }
}

/**
 * Image removal options for compose down.
 */
enum class RemoveImages(val value: String) {
    /** Remove all images. */
    ALL("all"),

    /** Remove only locally built images. */
    LOCAL("local")
}
