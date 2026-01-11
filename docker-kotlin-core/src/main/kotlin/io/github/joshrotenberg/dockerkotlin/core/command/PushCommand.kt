package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to upload an image to a registry.
 *
 * Equivalent to `docker push`.
 *
 * Example usage:
 * ```kotlin
 * PushCommand("registry.example.com/my-image:v1").execute()
 * ```
 */
class PushCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var allTags: Boolean = false
    private var disableContentTrust: Boolean = true
    private var quiet: Boolean = false

    /** Push all tagged images in the repository. */
    fun allTags() = apply { this.allTags = true }

    /** Skip image signing. */
    fun disableContentTrust(disable: Boolean) = apply { this.disableContentTrust = disable }

    /** Suppress verbose output. */
    fun quiet() = apply { this.quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("push")
        if (allTags) add("--all-tags")
        if (!disableContentTrust) add("--disable-content-trust=false")
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
        fun builder(image: String): PushCommand = PushCommand(image)
    }
}
