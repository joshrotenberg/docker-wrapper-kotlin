package io.github.joshrotenberg.dockerkotlin.core

import io.github.joshrotenberg.dockerkotlin.core.command.PullCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StopCommand
import io.github.joshrotenberg.dockerkotlin.core.command.VersionCommand
import io.github.joshrotenberg.dockerkotlin.core.command.VersionInfo
import io.github.joshrotenberg.dockerkotlin.core.command.ContainerId
import io.github.joshrotenberg.dockerkotlin.core.platform.PlatformInfo

/**
 * Main entry point for Docker operations.
 *
 * Provides a fluent API for executing Docker commands with optional
 * platform detection and configuration.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val docker = Docker()
 *
 * // Run a container
 * val containerId = docker.run("nginx:alpine") {
 *     name("web-server")
 *     port(8080, 80)
 *     detach()
 * }
 *
 * // Stop and remove
 * docker.stop(containerId.value)
 * docker.rm(containerId.value)
 * ```
 *
 * Example usage (Java):
 * ```java
 * Docker docker = new Docker();
 *
 * ContainerId containerId = docker.run("nginx:alpine", cmd -> {
 *     cmd.name("web-server");
 *     cmd.port(8080, 80);
 *     cmd.detach();
 *     return cmd;
 * });
 * ```
 */
class Docker(
    private val config: DockerConfig = DockerConfig()
) {
    private val executor: CommandExecutor by lazy {
        CommandExecutor(
            platformInfo = if (config.detectPlatform) PlatformInfo.detect() else null,
            defaultTimeout = config.defaultTimeout
        )
    }

    /**
     * Get Docker version information.
     */
    suspend fun version(): VersionInfo = VersionCommand(executor).execute()

    /**
     * Get Docker version information (blocking).
     */
    fun versionBlocking(): VersionInfo = VersionCommand(executor).executeBlocking()

    /**
     * Run a container with the given image.
     *
     * @param image The image to run
     * @param configure Configuration block for the run command
     * @return The container ID
     */
    suspend fun run(image: String, configure: RunCommand.() -> Unit = {}): ContainerId {
        return RunCommand(image, executor).apply(configure).execute()
    }

    /**
     * Run a container with the given image (blocking).
     */
    fun runBlocking(image: String, configure: RunCommand.() -> Unit = {}): ContainerId {
        return RunCommand(image, executor).apply(configure).executeBlocking()
    }

    /**
     * Stop a running container.
     *
     * @param container Container ID or name
     * @param configure Configuration block for the stop command
     */
    suspend fun stop(container: String, configure: StopCommand.() -> Unit = {}) {
        StopCommand(container, executor).apply(configure).execute()
    }

    /**
     * Stop a running container (blocking).
     */
    fun stopBlocking(container: String, configure: StopCommand.() -> Unit = {}) {
        StopCommand(container, executor).apply(configure).executeBlocking()
    }

    /**
     * Remove a container.
     *
     * @param container Container ID or name
     * @param configure Configuration block for the rm command
     */
    suspend fun rm(container: String, configure: RmCommand.() -> Unit = {}) {
        RmCommand(container, executor).apply(configure).execute()
    }

    /**
     * Remove a container (blocking).
     */
    fun rmBlocking(container: String, configure: RmCommand.() -> Unit = {}) {
        RmCommand(container, executor).apply(configure).executeBlocking()
    }

    /**
     * Pull an image from a registry.
     *
     * @param image The image to pull
     * @param configure Configuration block for the pull command
     */
    suspend fun pull(image: String, configure: PullCommand.() -> Unit = {}) {
        PullCommand(image, executor).apply(configure).execute()
    }

    /**
     * Pull an image from a registry (blocking).
     */
    fun pullBlocking(image: String, configure: PullCommand.() -> Unit = {}) {
        PullCommand(image, executor).apply(configure).executeBlocking()
    }

    companion object {
        /**
         * Create a Docker instance with default configuration.
         */
        @JvmStatic
        fun create(): Docker = Docker()

        /**
         * Create a Docker instance with custom configuration.
         */
        @JvmStatic
        fun create(configure: DockerConfig.() -> Unit): Docker {
            return Docker(DockerConfig().apply(configure))
        }
    }
}

/**
 * Kotlin DSL entry point for Docker operations.
 */
fun docker(configure: DockerConfig.() -> Unit = {}): Docker {
    return Docker(DockerConfig().apply(configure))
}
