package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display an image manifest or manifest list.
 *
 * Equivalent to `docker manifest inspect`.
 *
 * Note: This is an experimental Docker feature.
 *
 * Example usage:
 * ```kotlin
 * val manifest = ManifestInspectCommand("myrepo/myimage:latest")
 *     .verbose()
 *     .execute()
 * ```
 */
class ManifestInspectCommand(
    private val manifest: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var manifestList: String? = null
    private var insecure = false
    private var verbose = false

    /** Set the manifest list (optional, for inspecting a specific manifest within a list). */
    fun manifestList(list: String) = apply { manifestList = list }

    /** Allow communication with an insecure registry. */
    fun insecure() = apply { insecure = true }

    /** Output additional info including layers and platform. */
    fun verbose() = apply { verbose = true }

    override fun buildArgs(): List<String> = buildList {
        add("manifest")
        add("inspect")
        if (insecure) add("--insecure")
        if (verbose) add("--verbose")
        manifestList?.let { add(it) }
        add(manifest)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(manifest: String): ManifestInspectCommand = ManifestInspectCommand(manifest)
    }
}
