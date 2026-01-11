package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to copy files/folders between a container and the local filesystem.
 *
 * Equivalent to `docker cp`.
 *
 * Example usage:
 * ```kotlin
 * // Copy from container to host
 * CpCommand.fromContainer("my-container", "/app/data", "/tmp/data").execute()
 *
 * // Copy from host to container
 * CpCommand.toContainer("/tmp/data", "my-container", "/app/data").execute()
 * ```
 */
class CpCommand private constructor(
    private val source: String,
    private val destination: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var archive: Boolean = false
    private var followLink: Boolean = false
    private var quiet: Boolean = false

    /** Archive mode (copy all uid/gid information). */
    fun archive() = apply { this.archive = true }

    /** Always follow symbol link in SRC_PATH. */
    fun followLink() = apply { this.followLink = true }

    /** Suppress progress output during copy. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("cp")
        if (archive) add("--archive")
        if (followLink) add("--follow-link")
        if (quiet) add("--quiet")
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
        /**
         * Copy from container to local filesystem.
         *
         * @param container The container name or ID
         * @param containerPath The path inside the container
         * @param hostPath The path on the host
         */
        @JvmStatic
        fun fromContainer(
            container: String,
            containerPath: String,
            hostPath: String,
            executor: CommandExecutor = CommandExecutor()
        ): CpCommand = CpCommand("$container:$containerPath", hostPath, executor)

        /**
         * Copy from local filesystem to container.
         *
         * @param hostPath The path on the host
         * @param container The container name or ID
         * @param containerPath The path inside the container
         */
        @JvmStatic
        fun toContainer(
            hostPath: String,
            container: String,
            containerPath: String,
            executor: CommandExecutor = CommandExecutor()
        ): CpCommand = CpCommand(hostPath, "$container:$containerPath", executor)

        /**
         * Generic builder for source and destination.
         */
        @JvmStatic
        fun builder(source: String, destination: String): CpCommand =
            CpCommand(source, destination)
    }
}
