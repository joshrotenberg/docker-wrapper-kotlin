package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a local manifest list for annotating and pushing to a registry.
 *
 * Equivalent to `docker manifest create`.
 *
 * Note: This is an experimental Docker feature.
 *
 * Example usage:
 * ```kotlin
 * ManifestCreateCommand("myrepo/myimage:latest")
 *     .manifest("myrepo/myimage:amd64")
 *     .manifest("myrepo/myimage:arm64")
 *     .execute()
 * ```
 */
class ManifestCreateCommand(
    private val manifestList: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private val manifests = mutableListOf<String>()
    private var amend = false
    private var insecure = false

    /** Add a manifest to include in the manifest list. */
    fun manifest(manifest: String) = apply { manifests.add(manifest) }

    /** Add multiple manifests to include in the manifest list. */
    fun manifests(vararg manifests: String) = apply { this.manifests.addAll(manifests) }

    /** Amend an existing manifest list. */
    fun amend() = apply { amend = true }

    /** Allow communication with an insecure registry. */
    fun insecure() = apply { insecure = true }

    override fun buildArgs(): List<String> = buildList {
        add("manifest")
        add("create")
        if (amend) add("--amend")
        if (insecure) add("--insecure")
        add(manifestList)
        addAll(manifests)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(manifestList: String): ManifestCreateCommand = ManifestCreateCommand(manifestList)
    }
}
