package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to remove one or more images.
 *
 * Equivalent to `docker rmi`.
 *
 * Example usage:
 * ```kotlin
 * RmiCommand("my-image:old").execute()
 * RmiCommand(listOf("image1", "image2")).force().execute()
 * ```
 */
class RmiCommand(
    private val images: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<String>>(executor) {

    constructor(image: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(image), executor)

    private var force: Boolean = false
    private var noPrune: Boolean = false

    /** Force removal of the image. */
    fun force() = apply { this.force = true }

    /** Do not delete untagged parent images. */
    fun noPrune() = apply { this.noPrune = true }

    override fun buildArgs(): List<String> = buildList {
        add("rmi")
        if (force) add("--force")
        if (noPrune) add("--no-prune")
        addAll(images)
    }

    override suspend fun execute(): List<String> {
        val output = executeRaw()
        return parseOutput(output.stdout)
    }

    override fun executeBlocking(): List<String> {
        val output = executeRawBlocking()
        return parseOutput(output.stdout)
    }

    private fun parseOutput(stdout: String): List<String> {
        // Returns deleted image IDs
        val regex = Regex("""Deleted: sha256:([a-f0-9]+)""")
        return regex.findAll(stdout).map { it.groupValues[1] }.toList()
    }

    companion object {
        @JvmStatic
        fun builder(image: String): RmiCommand = RmiCommand(image)

        @JvmStatic
        fun builder(images: List<String>): RmiCommand = RmiCommand(images)
    }
}
