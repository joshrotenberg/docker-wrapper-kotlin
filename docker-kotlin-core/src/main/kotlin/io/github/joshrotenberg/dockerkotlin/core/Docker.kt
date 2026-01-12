package io.github.joshrotenberg.dockerkotlin.core

import io.github.joshrotenberg.dockerkotlin.core.command.BuildCommand
import io.github.joshrotenberg.dockerkotlin.core.command.ContainerId
import io.github.joshrotenberg.dockerkotlin.core.command.ExecCommand
import io.github.joshrotenberg.dockerkotlin.core.command.ExecOutput
import io.github.joshrotenberg.dockerkotlin.core.command.ImagesCommand
import io.github.joshrotenberg.dockerkotlin.core.command.InfoCommand
import io.github.joshrotenberg.dockerkotlin.core.command.InspectCommand
import io.github.joshrotenberg.dockerkotlin.core.command.LogsCommand
import io.github.joshrotenberg.dockerkotlin.core.command.PsCommand
import io.github.joshrotenberg.dockerkotlin.core.command.PullCommand
import io.github.joshrotenberg.dockerkotlin.core.command.PushCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RmiCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StartCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StopCommand
import io.github.joshrotenberg.dockerkotlin.core.command.TagCommand
import io.github.joshrotenberg.dockerkotlin.core.command.VersionCommand
import io.github.joshrotenberg.dockerkotlin.core.command.network.NetworkCreateCommand
import io.github.joshrotenberg.dockerkotlin.core.command.network.NetworkLsCommand
import io.github.joshrotenberg.dockerkotlin.core.command.network.NetworkRmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.volume.VolumeCreateCommand
import io.github.joshrotenberg.dockerkotlin.core.command.volume.VolumeLsCommand
import io.github.joshrotenberg.dockerkotlin.core.command.volume.VolumeRmCommand
import io.github.joshrotenberg.dockerkotlin.core.model.ContainerSummary
import io.github.joshrotenberg.dockerkotlin.core.model.DockerInfo
import io.github.joshrotenberg.dockerkotlin.core.model.DockerVersion
import io.github.joshrotenberg.dockerkotlin.core.model.ImageSummary
import io.github.joshrotenberg.dockerkotlin.core.model.NetworkSummary
import io.github.joshrotenberg.dockerkotlin.core.model.VolumeSummary
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

    // ========== System Commands ==========

    /**
     * Get Docker version information.
     */
    suspend fun version(): DockerVersion = VersionCommand(executor).execute()

    /**
     * Get Docker version information (blocking).
     */
    fun versionBlocking(): DockerVersion = VersionCommand(executor).executeBlocking()

    /**
     * Get Docker system information.
     */
    suspend fun info(): DockerInfo = InfoCommand(executor).execute()

    /**
     * Get Docker system information (blocking).
     */
    fun infoBlocking(): DockerInfo = InfoCommand(executor).executeBlocking()

    // ========== Container Lifecycle ==========

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
     * Start a stopped container.
     *
     * @param container Container ID or name
     * @param configure Configuration block for the start command
     */
    suspend fun start(container: String, configure: StartCommand.() -> Unit = {}) {
        StartCommand(container, executor).apply(configure).execute()
    }

    /**
     * Start a stopped container (blocking).
     */
    fun startBlocking(container: String, configure: StartCommand.() -> Unit = {}) {
        StartCommand(container, executor).apply(configure).executeBlocking()
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
     * List containers.
     *
     * @param configure Configuration block for the ps command
     * @return List of containers
     */
    suspend fun ps(configure: PsCommand.() -> Unit = {}): List<ContainerSummary> {
        return PsCommand(executor).apply(configure).execute()
    }

    /**
     * List containers (blocking).
     */
    fun psBlocking(configure: PsCommand.() -> Unit = {}): List<ContainerSummary> {
        return PsCommand(executor).apply(configure).executeBlocking()
    }

    /**
     * Execute a command in a running container.
     *
     * @param container Container ID or name
     * @param command Command to execute
     * @param configure Configuration block for the exec command
     * @return Execution output
     */
    suspend fun exec(
        container: String,
        vararg command: String,
        configure: ExecCommand.() -> Unit = {}
    ): ExecOutput {
        return ExecCommand(container, command.toList(), executor).apply(configure).execute()
    }

    /**
     * Execute a command in a running container (blocking).
     */
    fun execBlocking(
        container: String,
        vararg command: String,
        configure: ExecCommand.() -> Unit = {}
    ): ExecOutput {
        return ExecCommand(container, command.toList(), executor).apply(configure).executeBlocking()
    }

    /**
     * Fetch container logs.
     *
     * @param container Container ID or name
     * @param configure Configuration block for the logs command
     * @return Log output
     */
    suspend fun logs(container: String, configure: LogsCommand.() -> Unit = {}): String {
        return LogsCommand(container, executor).apply(configure).execute()
    }

    /**
     * Fetch container logs (blocking).
     */
    fun logsBlocking(container: String, configure: LogsCommand.() -> Unit = {}): String {
        return LogsCommand(container, executor).apply(configure).executeBlocking()
    }

    /**
     * Inspect a container or image.
     *
     * @param obj Object to inspect (container ID, image, etc.)
     * @param configure Configuration block for the inspect command
     * @return JSON inspection output
     */
    suspend fun inspect(obj: String, configure: InspectCommand.() -> Unit = {}): String {
        return InspectCommand(obj, executor).apply(configure).execute()
    }

    /**
     * Inspect a container or image (blocking).
     */
    fun inspectBlocking(obj: String, configure: InspectCommand.() -> Unit = {}): String {
        return InspectCommand(obj, executor).apply(configure).executeBlocking()
    }

    // ========== Image Commands ==========

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

    /**
     * Push an image to a registry.
     *
     * @param image The image to push
     * @param configure Configuration block for the push command
     */
    suspend fun push(image: String, configure: PushCommand.() -> Unit = {}) {
        PushCommand(image, executor).apply(configure).execute()
    }

    /**
     * Push an image to a registry (blocking).
     */
    fun pushBlocking(image: String, configure: PushCommand.() -> Unit = {}) {
        PushCommand(image, executor).apply(configure).executeBlocking()
    }

    /**
     * Build an image from a Dockerfile.
     *
     * @param context Build context path
     * @param configure Configuration block for the build command
     * @return Build output
     */
    suspend fun build(context: String, configure: BuildCommand.() -> Unit = {}): String {
        return BuildCommand(context, executor).apply(configure).execute()
    }

    /**
     * Build an image from a Dockerfile (blocking).
     */
    fun buildBlocking(context: String, configure: BuildCommand.() -> Unit = {}): String {
        return BuildCommand(context, executor).apply(configure).executeBlocking()
    }

    /**
     * List images.
     *
     * @param configure Configuration block for the images command
     * @return List of images
     */
    suspend fun images(configure: ImagesCommand.() -> Unit = {}): List<ImageSummary> {
        return ImagesCommand(executor = executor).apply(configure).execute()
    }

    /**
     * List images (blocking).
     */
    fun imagesBlocking(configure: ImagesCommand.() -> Unit = {}): List<ImageSummary> {
        return ImagesCommand(executor = executor).apply(configure).executeBlocking()
    }

    /**
     * Remove an image.
     *
     * @param image Image ID or name
     * @param configure Configuration block for the rmi command
     */
    suspend fun rmi(image: String, configure: RmiCommand.() -> Unit = {}) {
        RmiCommand(image, executor).apply(configure).execute()
    }

    /**
     * Remove an image (blocking).
     */
    fun rmiBlocking(image: String, configure: RmiCommand.() -> Unit = {}) {
        RmiCommand(image, executor).apply(configure).executeBlocking()
    }

    /**
     * Tag an image.
     *
     * @param source Source image
     * @param target Target image tag
     */
    suspend fun tag(source: String, target: String) {
        TagCommand(source, target, executor).execute()
    }

    /**
     * Tag an image (blocking).
     */
    fun tagBlocking(source: String, target: String) {
        TagCommand(source, target, executor).executeBlocking()
    }

    // ========== Network Commands ==========

    /** Network management commands. */
    val network: NetworkCommands = NetworkCommands()

    inner class NetworkCommands {
        /**
         * Create a network.
         *
         * @param name Network name
         * @param configure Configuration block
         * @return Network ID
         */
        suspend fun create(name: String, configure: NetworkCreateCommand.() -> Unit = {}): String {
            return NetworkCreateCommand(name, executor).apply(configure).execute()
        }

        /**
         * Create a network (blocking).
         */
        fun createBlocking(name: String, configure: NetworkCreateCommand.() -> Unit = {}): String {
            return NetworkCreateCommand(name, executor).apply(configure).executeBlocking()
        }

        /**
         * List networks.
         *
         * @param configure Configuration block
         * @return List of networks
         */
        suspend fun ls(configure: NetworkLsCommand.() -> Unit = {}): List<NetworkSummary> {
            return NetworkLsCommand(executor).apply(configure).execute()
        }

        /**
         * List networks (blocking).
         */
        fun lsBlocking(configure: NetworkLsCommand.() -> Unit = {}): List<NetworkSummary> {
            return NetworkLsCommand(executor).apply(configure).executeBlocking()
        }

        /**
         * Remove a network.
         *
         * @param name Network name or ID
         * @param configure Configuration block
         */
        suspend fun rm(name: String, configure: NetworkRmCommand.() -> Unit = {}) {
            NetworkRmCommand(name, executor).apply(configure).execute()
        }

        /**
         * Remove a network (blocking).
         */
        fun rmBlocking(name: String, configure: NetworkRmCommand.() -> Unit = {}) {
            NetworkRmCommand(name, executor).apply(configure).executeBlocking()
        }
    }

    // ========== Volume Commands ==========

    /** Volume management commands. */
    val volume: VolumeCommands = VolumeCommands()

    inner class VolumeCommands {
        /**
         * Create a volume.
         *
         * @param configure Configuration block
         * @return Volume name
         */
        suspend fun create(configure: VolumeCreateCommand.() -> Unit = {}): String {
            return VolumeCreateCommand(executor).apply(configure).execute()
        }

        /**
         * Create a volume (blocking).
         */
        fun createBlocking(configure: VolumeCreateCommand.() -> Unit = {}): String {
            return VolumeCreateCommand(executor).apply(configure).executeBlocking()
        }

        /**
         * List volumes.
         *
         * @param configure Configuration block
         * @return List of volumes
         */
        suspend fun ls(configure: VolumeLsCommand.() -> Unit = {}): List<VolumeSummary> {
            return VolumeLsCommand(executor).apply(configure).execute()
        }

        /**
         * List volumes (blocking).
         */
        fun lsBlocking(configure: VolumeLsCommand.() -> Unit = {}): List<VolumeSummary> {
            return VolumeLsCommand(executor).apply(configure).executeBlocking()
        }

        /**
         * Remove a volume.
         *
         * @param name Volume name
         * @param configure Configuration block
         */
        suspend fun rm(name: String, configure: VolumeRmCommand.() -> Unit = {}) {
            VolumeRmCommand(name, executor).apply(configure).execute()
        }

        /**
         * Remove a volume (blocking).
         */
        fun rmBlocking(name: String, configure: VolumeRmCommand.() -> Unit = {}) {
            VolumeRmCommand(name, executor).apply(configure).executeBlocking()
        }
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
