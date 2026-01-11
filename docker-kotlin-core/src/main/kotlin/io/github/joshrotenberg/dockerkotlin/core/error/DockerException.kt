package io.github.joshrotenberg.dockerkotlin.core.error

import kotlin.time.Duration

/**
 * Base exception for all Docker-related errors.
 */
sealed class DockerException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Docker executable was not found in PATH.
     */
    class DockerNotFound(
        message: String = "Docker executable not found in PATH"
    ) : DockerException(message)

    /**
     * Docker daemon is not running or not accessible.
     */
    class DaemonNotRunning(
        message: String = "Docker daemon is not running"
    ) : DockerException(message)

    /**
     * A Docker command failed with a non-zero exit code.
     */
    class CommandFailed(
        val command: String,
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    ) : DockerException(
        "Command '$command' failed with exit code $exitCode: ${stderr.ifBlank { stdout }.take(200)}"
    )

    /**
     * A Docker command timed out.
     */
    class Timeout(
        val duration: Duration
    ) : DockerException("Command timed out after $duration")

    /**
     * Container was not found.
     */
    class ContainerNotFound(
        val containerId: String
    ) : DockerException("Container not found: $containerId")

    /**
     * Image was not found.
     */
    class ImageNotFound(
        val image: String
    ) : DockerException("Image not found: $image")

    /**
     * Network was not found.
     */
    class NetworkNotFound(
        val network: String
    ) : DockerException("Network not found: $network")

    /**
     * Volume was not found.
     */
    class VolumeNotFound(
        val volume: String
    ) : DockerException("Volume not found: $volume")

    /**
     * Invalid configuration provided.
     */
    class InvalidConfig(
        message: String
    ) : DockerException(message)

    /**
     * Generic Docker error for unexpected cases.
     */
    class Generic(
        message: String,
        cause: Throwable? = null
    ) : DockerException(message, cause)

    /**
     * A command or feature is not supported by the current runtime.
     */
    class UnsupportedByRuntime(
        val command: String,
        val runtime: String,
        message: String = "Command '$command' is not supported by $runtime"
    ) : DockerException(message)
}
