package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Docker version information.
 */
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

/**
 * Command to get Docker version information.
 *
 * Equivalent to `docker version`.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val version = VersionCommand().execute()
 * println("Client: ${version.clientVersion}")
 * println("Server: ${version.serverVersion}")
 * ```
 *
 * Example usage (Java):
 * ```java
 * VersionInfo version = new VersionCommand().executeBlocking();
 * System.out.println("Client: " + version.getClientVersion());
 * ```
 */
class VersionCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<VersionInfo>(executor) {

    override fun buildArgs(): List<String> = listOf("version")

    override suspend fun execute(): VersionInfo {
        val output = executeRaw()
        return VersionInfo.parse(output.stdout)
    }

    override fun executeBlocking(): VersionInfo {
        val output = executeRawBlocking()
        return VersionInfo.parse(output.stdout)
    }

    companion object {
        /**
         * Create a new VersionCommand.
         */
        @JvmStatic
        fun builder(): VersionCommand = VersionCommand()
    }
}
