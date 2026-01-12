package io.github.joshrotenberg.dockerkotlin.core.command.image

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more images.
 *
 * Equivalent to `docker image inspect`.
 *
 * Example usage:
 * ```kotlin
 * ImageInspectCommand("nginx:latest").executeBlocking()
 * ImageInspectCommand("nginx:latest").format("{{.Id}}").executeBlocking()
 * ```
 */
class ImageInspectCommand : AbstractDockerCommand<String> {

    private val images: List<String>
    private var format: String? = null

    constructor(image: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.images = listOf(image)
    }

    constructor(images: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.images = images
    }

    /** Set output format using Go template. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("image")
        add("inspect")
        format?.let { add("--format"); add(it) }
        addAll(images)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(image: String): ImageInspectCommand = ImageInspectCommand(image)

        @JvmStatic
        fun builder(images: List<String>): ImageInspectCommand = ImageInspectCommand(images)
    }
}
