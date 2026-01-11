package io.github.joshrotenberg.dockerkotlin.core.platform

/**
 * Docker-compatible runtimes.
 */
enum class Runtime(
    /** The CLI command to invoke this runtime. */
    val command: String,
    /** Whether this runtime supports BuildKit builder instance management (create, ls, rm, use, stop). */
    val supportsBuilderInstances: Boolean = true,
    /** Whether this runtime uses Docker-compatible output formats. */
    val dockerCompatibleOutput: Boolean = true
) {
    /** Standard Docker. */
    DOCKER("docker"),

    /** Podman container runtime. Uses buildah for builds, no builder instance management. */
    PODMAN("podman", supportsBuilderInstances = false, dockerCompatibleOutput = false),

    /** Colima (macOS). */
    COLIMA("docker"),

    /** OrbStack (macOS). */
    ORBSTACK("docker"),

    /** Rancher Desktop. */
    RANCHER_DESKTOP("docker"),

    /** Docker Desktop. */
    DOCKER_DESKTOP("docker");

    /** Whether this is a Podman-based runtime. */
    val isPodman: Boolean get() = this == PODMAN

    /** Whether this is a Docker-based runtime (uses docker CLI). */
    val isDocker: Boolean get() = command == "docker"

    /**
     * Check if a specific builder command is supported.
     * Podman only supports: build, inspect, prune, version
     * Docker supports all builder commands including instance management.
     */
    fun supportsBuilderCommand(subcommand: String): Boolean {
        if (supportsBuilderInstances) return true
        // Podman buildx only supports these commands
        return subcommand in listOf("build", "inspect", "prune", "version")
    }

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
