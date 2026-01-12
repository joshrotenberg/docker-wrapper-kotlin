package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to copy files/folders between a service container and the local filesystem.
 *
 * Equivalent to `docker compose cp`.
 *
 * Example usage:
 * ```kotlin
 * // Copy from container to host
 * ComposeCpCommand("web:/app/config.json", "./config.json")
 *     .file("docker-compose.yml")
 *     .execute()
 *
 * // Copy from host to container
 * ComposeCpCommand("./config.json", "web:/app/config.json")
 *     .file("docker-compose.yml")
 *     .execute()
 * ```
 */
class ComposeCpCommand(
    private val source: String,
    private val destination: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeCpCommand>(executor) {

    private var archive = false
    private var followLink = false
    private var index = 0

    /** Archive mode (copy all uid/gid information). */
    fun archive() = apply { archive = true }

    /** Follow symlinks in source path. */
    fun followLink() = apply { followLink = true }

    /** Index of the container if service has multiple replicas. */
    fun index(index: Int) = apply { this.index = index }

    override fun subcommand(): String = "cp"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (archive) add("--archive")
        if (followLink) add("--follow-link")
        if (index > 0) {
            add("--index"); add(index.toString())
        }
        add(source)
        add(destination)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(source: String, destination: String): ComposeCpCommand =
            ComposeCpCommand(source, destination)
    }
}
