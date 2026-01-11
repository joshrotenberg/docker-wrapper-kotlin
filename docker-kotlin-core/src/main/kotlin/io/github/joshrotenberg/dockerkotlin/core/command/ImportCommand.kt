package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to import the contents from a tarball to create a filesystem image.
 *
 * Equivalent to `docker import`.
 *
 * Example usage:
 * ```kotlin
 * val imageId = ImportCommand("/tmp/container.tar", "my-image:imported").execute()
 * ```
 */
class ImportCommand(
    private val file: String,
    private val repository: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val changes = mutableListOf<String>()
    private var message: String? = null
    private var platform: String? = null

    /** Apply Dockerfile instruction to the created image. */
    fun change(instruction: String) = apply { this.changes.add(instruction) }

    /** Set commit message for imported image. */
    fun message(message: String) = apply { this.message = message }

    /** Set platform if server is multi-platform capable. */
    fun platform(platform: String) = apply { this.platform = platform }

    override fun buildArgs(): List<String> = buildList {
        add("import")
        changes.forEach { add("--change"); add(it) }
        message?.let { add("--message"); add(it) }
        platform?.let { add("--platform"); add(it) }
        add(file)
        repository?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(file: String): ImportCommand = ImportCommand(file)

        @JvmStatic
        fun builder(file: String, repository: String): ImportCommand =
            ImportCommand(file, repository)
    }
}
