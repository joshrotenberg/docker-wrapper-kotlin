package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to create a new image from a container's changes.
 *
 * Equivalent to `docker commit`.
 *
 * Example usage:
 * ```kotlin
 * val imageId = CommitCommand("my-container", "my-image:v1").execute()
 * val imageId = CommitCommand("my-container", "my-image:v1")
 *     .author("John Doe")
 *     .message("Added new files")
 *     .execute()
 * ```
 */
class CommitCommand(
    private val container: String,
    private val repository: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var author: String? = null
    private val changes = mutableListOf<String>()
    private var message: String? = null
    private var pause: Boolean = true

    /** Author (e.g., "John Hannibal Smith <hannibal@a-team.com>"). */
    fun author(author: String) = apply { this.author = author }

    /** Apply Dockerfile instruction to the created image. */
    fun change(instruction: String) = apply { this.changes.add(instruction) }

    /** Commit message. */
    fun message(message: String) = apply { this.message = message }

    /** Pause container during commit (default true). */
    fun pause(pause: Boolean) = apply { this.pause = pause }

    override fun buildArgs(): List<String> = buildList {
        add("commit")
        author?.let { add("--author"); add(it) }
        changes.forEach { add("--change"); add(it) }
        message?.let { add("--message"); add(it) }
        if (!pause) add("--pause=false")
        add(container)
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
        fun builder(container: String): CommitCommand = CommitCommand(container)

        @JvmStatic
        fun builder(container: String, repository: String): CommitCommand =
            CommitCommand(container, repository)
    }
}
