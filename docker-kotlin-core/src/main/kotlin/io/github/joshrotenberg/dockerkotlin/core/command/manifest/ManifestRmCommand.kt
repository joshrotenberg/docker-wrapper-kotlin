package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to delete one or more manifest lists from local storage.
 *
 * Equivalent to `docker manifest rm`.
 *
 * Note: This is an experimental Docker feature.
 *
 * Example usage:
 * ```kotlin
 * ManifestRmCommand()
 *     .manifestLists("myrepo/myimage:v1", "myrepo/myimage:v2")
 *     .execute()
 * ```
 */
class ManifestRmCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private val manifestLists = mutableListOf<String>()

    /** Add a manifest list to remove. */
    fun manifestList(list: String) = apply { manifestLists.add(list) }

    /** Add multiple manifest lists to remove. */
    fun manifestLists(vararg lists: String) = apply { manifestLists.addAll(lists) }

    override fun buildArgs(): List<String> = buildList {
        add("manifest")
        add("rm")
        addAll(manifestLists)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): ManifestRmCommand = ManifestRmCommand()

        @JvmStatic
        fun builder(manifestList: String): ManifestRmCommand = ManifestRmCommand().manifestList(manifestList)
    }
}
