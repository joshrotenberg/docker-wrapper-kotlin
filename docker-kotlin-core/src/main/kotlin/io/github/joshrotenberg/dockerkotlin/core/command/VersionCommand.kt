package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.model.DockerVersion
import kotlinx.serialization.json.Json

/**
 * Docker version information.
 *
 * @deprecated Use [DockerVersion] instead for typed JSON responses.
 */
@Deprecated("Use DockerVersion instead", ReplaceWith("DockerVersion"))
data class VersionInfo(
    val clientVersion: String,
    val serverVersion: String?,
    val apiVersion: String?,
    val goVersion: String?,
    val os: String?,
    val arch: String?
) {
    companion object {
        internal fun parse(output: String): VersionInfo {
            val lines = output.lines()
            var clientVersion = "unknown"
            var serverVersion: String? = null
            var apiVersion: String? = null
            var goVersion: String? = null
            var os: String? = null
            var arch: String? = null

            var inClient = false
            var inServer = false

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("Client:") -> {
                        inClient = true
                        inServer = false
                    }

                    trimmed.startsWith("Server:") -> {
                        inClient = false
                        inServer = true
                    }

                    trimmed.startsWith("Version:") -> {
                        val version = trimmed.removePrefix("Version:").trim()
                        if (inClient) clientVersion = version
                        else if (inServer) serverVersion = version
                    }

                    trimmed.startsWith("API version:") -> {
                        if (inClient) apiVersion = trimmed.removePrefix("API version:").trim()
                    }

                    trimmed.startsWith("Go version:") -> {
                        if (inClient) goVersion = trimmed.removePrefix("Go version:").trim()
                    }

                    trimmed.startsWith("OS/Arch:") -> {
                        val parts = trimmed.removePrefix("OS/Arch:").trim().split("/")
                        if (inClient && parts.size >= 2) {
                            os = parts[0]
                            arch = parts[1]
                        }
                    }
                }
            }

            return VersionInfo(
                clientVersion = clientVersion,
                serverVersion = serverVersion,
                apiVersion = apiVersion,
                goVersion = goVersion,
                os = os,
                arch = arch
            )
        }
    }
}

private val json = Json { ignoreUnknownKeys = true }

/**
 * Command to get Docker version information.
 *
 * Equivalent to `docker version`.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val version = VersionCommand().execute()
 * println("Client: ${version.client.version}")
 * println("Server: ${version.server?.version}")
 * ```
 *
 * Example usage (Java):
 * ```java
 * DockerVersion version = new VersionCommand().executeBlocking();
 * System.out.println("Client: " + version.getClient().getVersion());
 * ```
 */
class VersionCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<DockerVersion>(executor) {

    override fun buildArgs(): List<String> = listOf("version", "--format", "json")

    override suspend fun execute(): DockerVersion {
        val output = executeRaw()
        return json.decodeFromString<DockerVersion>(output.stdout)
    }

    override fun executeBlocking(): DockerVersion {
        val output = executeRawBlocking()
        return json.decodeFromString<DockerVersion>(output.stdout)
    }

    /**
     * Execute and return the legacy VersionInfo format.
     */
    @Deprecated("Use execute() instead", ReplaceWith("execute()"))
    suspend fun executeLegacy(): VersionInfo {
        val version = execute()
        return VersionInfo(
            clientVersion = version.client.version,
            serverVersion = version.server?.version,
            apiVersion = version.client.apiVersion,
            goVersion = version.client.goVersion,
            os = version.client.os,
            arch = version.client.arch
        )
    }

    /**
     * Execute and return the legacy VersionInfo format (blocking).
     */
    @Deprecated("Use executeBlocking() instead", ReplaceWith("executeBlocking()"))
    fun executeLegacyBlocking(): VersionInfo {
        val version = executeBlocking()
        return VersionInfo(
            clientVersion = version.client.version,
            serverVersion = version.server?.version,
            apiVersion = version.client.apiVersion,
            goVersion = version.client.goVersion,
            os = version.client.os,
            arch = version.client.arch
        )
    }

    companion object {
        /**
         * Create a new VersionCommand.
         */
        @JvmStatic
        fun builder(): VersionCommand = VersionCommand()
    }
}
