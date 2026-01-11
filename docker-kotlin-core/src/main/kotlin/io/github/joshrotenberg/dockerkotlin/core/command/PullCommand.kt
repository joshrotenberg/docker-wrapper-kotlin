package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to pull an image from a registry.
 *
 * Equivalent to `docker pull`.
 *
 * Example usage:
 * ```kotlin
 * PullCommand("nginx:alpine").execute()
 * ```
 */
class PullCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var allTags = false
    private var platform: String? = null
    private var quiet = false

    /** Download all tagged images in the repository. */
    fun allTags() = apply { allTags = true }

    /** Set platform (e.g., "linux/amd64"). */
    fun platform(platform: String) = apply { this.platform = platform }

    /** Suppress verbose output. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("pull")
        if (allTags) add("--all-tags")
        platform?.let { add("--platform"); add(it) }
        if (quiet) add("--quiet")
        add(image)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(image: String): PullCommand = PullCommand(image)
    }
}
