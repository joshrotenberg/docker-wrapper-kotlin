package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to inspect a builder instance.
 *
 * Equivalent to `docker builder inspect` / `docker buildx inspect`.
 *
 * Example usage:
 * ```kotlin
 * val info = BuilderInspectCommand("my-builder").executeBlocking()
 * val current = BuilderInspectCommand().executeBlocking() // inspect current builder
 * ```
 */
class BuilderInspectCommand(
    private val name: String? = null,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var bootstrap = false
    private var format: String? = null

    /** Ensure builder is bootstrapped before inspecting. */
    fun bootstrap() = apply { bootstrap = true }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Output as JSON. */
    fun json() = format("json")

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("inspect")

        if (bootstrap) add("--bootstrap")
        format?.let { add("--format"); add(it) }

        name?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderInspectCommand = BuilderInspectCommand()

        @JvmStatic
        fun builder(name: String): BuilderInspectCommand = BuilderInspectCommand(name)
    }
}
