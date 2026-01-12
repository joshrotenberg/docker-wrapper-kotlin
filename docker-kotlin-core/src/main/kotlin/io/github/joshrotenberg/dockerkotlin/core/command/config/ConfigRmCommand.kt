package io.github.joshrotenberg.dockerkotlin.core.command.config

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more configs.
 *
 * Equivalent to `docker config rm`.
 *
 * Example usage:
 * ```kotlin
 * ConfigRmCommand("my-config").executeBlocking()
 * ConfigRmCommand(listOf("config1", "config2")).executeBlocking()
 * ```
 */
class ConfigRmCommand : AbstractDockerCommand<String> {

    private val configs: List<String>

    constructor(config: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.configs = listOf(config)
    }

    constructor(configs: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.configs = configs
    }

    override fun buildArgs(): List<String> = buildList {
        add("config")
        add("rm")
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
        fun builder(config: String): ConfigRmCommand = ConfigRmCommand(config)

        @JvmStatic
        fun builder(configs: List<String>): ConfigRmCommand = ConfigRmCommand(configs)
    }
}
