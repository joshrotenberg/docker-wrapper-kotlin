package io.github.joshrotenberg.dockerkotlin.core.platform

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Information about the Docker runtime environment.
 */
data class PlatformInfo(
    /** The Docker-compatible runtime being used. */
    val runtime: Runtime,
    /** The version of the runtime. */
    val version: String,
    /** The operating system platform. */
    val platform: Platform,
    /** Path to the Docker socket (if applicable). */
    val socketPath: Path?,
    /** Environment variables to set when running Docker commands. */
    val environmentVars: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Detect the current platform information.
         */
        fun detect(): PlatformInfo {
            val platform = Platform.detect()
            val runtime = Runtime.detect()
            val version = detectVersion(runtime)
            val socketPath = detectSocketPath(platform, runtime)
            val envVars = buildEnvironmentVars(platform, runtime, socketPath)

            return PlatformInfo(
                runtime = runtime,
                version = version,
                platform = platform,
                socketPath = socketPath,
                environmentVars = envVars
            )
        }

        private fun detectVersion(runtime: Runtime): String {
            return runCatching {
                val process = ProcessBuilder(runtime.command, "version", "--format", "{{.Client.Version}}")
                    .redirectErrorStream(true)
                    .start()
                process.inputStream.bufferedReader().readText().trim()
            }.getOrDefault("unknown")
        }

        private fun detectSocketPath(platform: Platform, runtime: Runtime): Path? {
            // Check DOCKER_HOST environment variable first
            System.getenv("DOCKER_HOST")?.let { host ->
                if (host.startsWith("unix://")) {
                    return Paths.get(host.removePrefix("unix://"))
                }
            }

            // Default socket paths
            return when (platform) {
                Platform.LINUX -> Paths.get("/var/run/docker.sock")
                Platform.MACOS -> when (runtime) {
                    Runtime.COLIMA -> {
                        val home = System.getProperty("user.home")
                        Paths.get(home, ".colima", "default", "docker.sock")
                    }

                    Runtime.ORBSTACK -> Paths.get("/var/run/docker.sock")
                    else -> Paths.get("/var/run/docker.sock")
                }

                Platform.WINDOWS -> null // Windows uses named pipes
                Platform.UNKNOWN -> null
            }
        }

        private fun buildEnvironmentVars(
            platform: Platform,
            runtime: Runtime,
            socketPath: Path?
        ): Map<String, String> {
            val vars = mutableMapOf<String, String>()

            // Set DOCKER_HOST if we have a non-default socket path
            if (socketPath != null && platform == Platform.MACOS && runtime == Runtime.COLIMA) {
                vars["DOCKER_HOST"] = "unix://${socketPath.toAbsolutePath()}"
            }

            return vars
        }
    }
}
