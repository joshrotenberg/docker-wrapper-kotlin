package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to create a tag TARGET_IMAGE that refers to SOURCE_IMAGE.
 *
 * Equivalent to `docker tag`.
 *
 * Example usage:
 * ```kotlin
 * TagCommand("my-image:latest", "registry.example.com/my-image:v1").execute()
 * ```
 */
class TagCommand(
    private val sourceImage: String,
    private val targetImage: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    override fun buildArgs(): List<String> = listOf("tag", sourceImage, targetImage)

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(sourceImage: String, targetImage: String): TagCommand =
            TagCommand(sourceImage, targetImage)
    }
}
