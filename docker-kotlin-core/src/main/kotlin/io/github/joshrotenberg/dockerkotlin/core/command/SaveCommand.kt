package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to save one or more images to a tar archive.
 *
 * Equivalent to `docker save`.
 *
 * Example usage:
 * ```kotlin
 * SaveCommand("my-image:latest").output("/tmp/image.tar").execute()
 * SaveCommand(listOf("image1", "image2")).output("/tmp/images.tar").execute()
 * ```
 */
class SaveCommand(
    private val images: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(image: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(image), executor)

    private var output: String? = null

    /** Write to a file, instead of STDOUT. */
    fun output(path: String) = apply { this.output = path }

    override fun buildArgs(): List<String> = buildList {
        add("save")
        output?.let { add("--output"); add(it) }
        addAll(images)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(image: String): SaveCommand = SaveCommand(image)

        @JvmStatic
        fun builder(images: List<String>): SaveCommand = SaveCommand(images)
    }
}
