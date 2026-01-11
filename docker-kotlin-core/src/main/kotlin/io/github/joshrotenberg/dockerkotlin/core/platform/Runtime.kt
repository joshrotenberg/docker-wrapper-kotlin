package io.github.joshrotenberg.dockerkotlin.core.platform

/**
 * Docker-compatible runtimes.
 */
enum class Runtime(
    /** The CLI command to invoke this runtime. */
    val command: String
) {
    /** Standard Docker. */
    DOCKER("docker"),

    /** Podman container runtime. */
    PODMAN("podman"),

    /** Colima (macOS). */
    COLIMA("docker"),

    /** OrbStack (macOS). */
    ORBSTACK("docker"),

    /** Rancher Desktop. */
    RANCHER_DESKTOP("docker"),

    /** Docker Desktop. */
    DOCKER_DESKTOP("docker");

    companion object {
        /**
         * Detect the runtime from the current environment.
         */
        fun detect(): Runtime {
            // Check for Podman first
            if (commandExists("podman")) {
                val result = runCatching {
                    ProcessBuilder("podman", "version")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor()
                }
                if (result.isSuccess && result.getOrNull() == 0) {
                    return PODMAN
                }
            }

            // Check Docker context for specific runtimes
            if (commandExists("docker")) {
                val contextOutput = runCatching {
                    val process = ProcessBuilder("docker", "context", "show")
                        .redirectErrorStream(true)
                        .start()
                    process.inputStream.bufferedReader().readText().trim()
                }.getOrNull()

                return when {
                    contextOutput?.contains("colima", ignoreCase = true) == true -> COLIMA
                    contextOutput?.contains("orbstack", ignoreCase = true) == true -> ORBSTACK
                    contextOutput?.contains("rancher", ignoreCase = true) == true -> RANCHER_DESKTOP
                    contextOutput?.contains("desktop", ignoreCase = true) == true -> DOCKER_DESKTOP
                    else -> DOCKER
                }
            }

            return DOCKER
        }

        private fun commandExists(command: String): Boolean {
            val whichCommand = if (System.getProperty("os.name").lowercase().contains("win")) {
                listOf("where", command)
            } else {
                listOf("which", command)
            }

            return runCatching {
                ProcessBuilder(whichCommand)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor() == 0
            }.getOrDefault(false)
        }
    }
}
