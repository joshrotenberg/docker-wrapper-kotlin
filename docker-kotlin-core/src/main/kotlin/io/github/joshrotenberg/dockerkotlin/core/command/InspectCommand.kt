package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to return low-level information on Docker objects.
 *
 * Equivalent to `docker inspect`.
 *
 * Example usage:
 * ```kotlin
 * val json = InspectCommand("my-container").execute()
 * val ip = InspectCommand("my-container")
 *     .format("{{.NetworkSettings.IPAddress}}")
 *     .execute()
 * ```
 */
class InspectCommand(
    private val objects: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    constructor(obj: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(obj), executor)

    private var format: String? = null
    private var showSize: Boolean = false
    private var type: String? = null

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Display total file sizes if the type is container. */
    fun size() = apply { this.showSize = true }

    /** Return JSON for specified type (container, image, network, volume, node, service, task). */
    fun type(type: String) = apply { this.type = type }

    override fun buildArgs(): List<String> = buildList {
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (showSize) add("--size")
        type?.let { add("--type"); add(it) }
        addAll(objects)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(obj: String): InspectCommand = InspectCommand(obj)

        @JvmStatic
        fun builder(objects: List<String>): InspectCommand = InspectCommand(objects)
    }
}
