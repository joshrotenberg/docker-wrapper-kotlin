package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to push a manifest list to a repository.
 *
 * Equivalent to `docker manifest push`.
 *
 * Note: This is an experimental Docker feature.
 *
 * Example usage:
 * ```kotlin
 * ManifestPushCommand("myrepo/myimage:latest")
 *     .purge()
 *     .execute()
 * ```
 */
class ManifestPushCommand(
    private val manifestList: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var insecure = false
    private var purge = false

    /** Allow push to an insecure registry. */
    fun insecure() = apply { insecure = true }

    /** Remove the local manifest list after push. */
    fun purge() = apply { purge = true }

    override fun buildArgs(): List<String> = buildList {
        add("manifest")
        add("push")
        if (insecure) add("--insecure")
        if (purge) add("--purge")
        add(manifestList)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(manifestList: String): ManifestPushCommand = ManifestPushCommand(manifestList)
    }
}
