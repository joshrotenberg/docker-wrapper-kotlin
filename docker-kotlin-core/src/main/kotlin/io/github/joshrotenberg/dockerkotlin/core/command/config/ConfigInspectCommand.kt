package io.github.joshrotenberg.dockerkotlin.core.command.config

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more configs.
 *
 * Equivalent to `docker config inspect`.
 *
 * Example usage:
 * ```kotlin
 * ConfigInspectCommand("my-config").executeBlocking()
 * ConfigInspectCommand("my-config").format("{{.ID}}").executeBlocking()
 * ConfigInspectCommand(listOf("config1", "config2")).executeBlocking()
 * ```
 */
class ConfigInspectCommand : AbstractDockerCommand<String> {

    private val configs: List<String>
    private var format: String? = null
    private var pretty = false

    constructor(config: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.configs = listOf(config)
    }

    constructor(configs: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.configs = configs
    }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Print the information in a human friendly format. */
    fun pretty() = apply { pretty = true }

    override fun buildArgs(): List<String> = buildList {
        add("config")
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (pretty) add("--pretty")
        addAll(configs)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(config: String): ConfigInspectCommand = ConfigInspectCommand(config)

        @JvmStatic
        fun builder(configs: List<String>): ConfigInspectCommand = ConfigInspectCommand(configs)
    }
}
