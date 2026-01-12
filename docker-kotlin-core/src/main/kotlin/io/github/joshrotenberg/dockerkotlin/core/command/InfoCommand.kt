package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.model.DockerInfo
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to display system-wide information.
 *
 * Equivalent to `docker info`.
 *
 * Example usage:
 * ```kotlin
 * val info = InfoCommand().execute()
 * println("Containers: ${info.containers}")
 * println("Images: ${info.images}")
 * println("Server Version: ${info.serverVersion}")
 * ```
 */
class InfoCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<DockerInfo>(executor) {

    override fun buildArgs(): List<String> = listOf("info", "--format", "json")

    override suspend fun execute(): DockerInfo {
        val output = executeRaw()
        return json.decodeFromString<DockerInfo>(output.stdout)
    }

    override fun executeBlocking(): DockerInfo {
        val output = executeRawBlocking()
        return json.decodeFromString<DockerInfo>(output.stdout)
    }

    companion object {
        @JvmStatic
        fun builder(): InfoCommand = InfoCommand()
    }
}
