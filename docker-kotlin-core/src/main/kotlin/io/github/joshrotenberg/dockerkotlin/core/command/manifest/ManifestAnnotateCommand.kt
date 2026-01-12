package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to add additional information to a local image manifest.
 *
 * Equivalent to `docker manifest annotate`.
 *
 * Note: This is an experimental Docker feature.
 *
 * Example usage:
 * ```kotlin
 * ManifestAnnotateCommand("myrepo/myimage:latest", "myrepo/myimage:arm64")
 *     .arch("arm64")
 *     .os("linux")
 *     .variant("v8")
 *     .execute()
 * ```
 */
class ManifestAnnotateCommand(
    private val manifestList: String,
    private val manifest: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var arch: String? = null
    private var os: String? = null
    private var osVersion: String? = null
    private val osFeatures = mutableListOf<String>()
    private var variant: String? = null

    /** Set the architecture. */
    fun arch(arch: String) = apply { this.arch = arch }

    /** Set the operating system. */
    fun os(os: String) = apply { this.os = os }

    /** Set the operating system version. */
    fun osVersion(version: String) = apply { osVersion = version }

    /** Add an operating system feature. */
    fun osFeature(feature: String) = apply { osFeatures.add(feature) }

    /** Set the architecture variant. */
    fun variant(variant: String) = apply { this.variant = variant }

    override fun buildArgs(): List<String> = buildList {
        add("manifest")
        add("annotate")
        arch?.let { add("--arch"); add(it) }
        os?.let { add("--os"); add(it) }
        osVersion?.let { add("--os-version"); add(it) }
        if (osFeatures.isNotEmpty()) {
            add("--os-features")
            add(osFeatures.joinToString(","))
        }
        variant?.let { add("--variant"); add(it) }
        add(manifestList)
        add(manifest)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(manifestList: String, manifest: String): ManifestAnnotateCommand =
            ManifestAnnotateCommand(manifestList, manifest)
    }
}
