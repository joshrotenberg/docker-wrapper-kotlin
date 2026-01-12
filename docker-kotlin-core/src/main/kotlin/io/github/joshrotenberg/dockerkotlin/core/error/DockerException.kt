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
     * Whether this exception represents a transient failure that may succeed on retry.
     * Override in subclasses to customize retry behavior.
     */
    open val isRetryable: Boolean = false

    /**
     * Docker executable was not found in PATH.
     */
    class DockerNotFound(
        message: String = "Docker executable not found in PATH"
    ) : DockerException(message)

    /**
     * Docker daemon is not running or not accessible.
     * This is retryable as the daemon may be restarting.
     */
    class DaemonNotRunning(
        message: String = "Docker daemon is not running"
    ) : DockerException(message) {
        override val isRetryable: Boolean = true
    }

    /**
     * A Docker command failed with a non-zero exit code.
     * Some failures are retryable based on error patterns (network issues, temporary conflicts).
     */
    class CommandFailed(
        val command: String,
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    ) : DockerException(
        "Command '$command' failed with exit code $exitCode: ${stderr.ifBlank { stdout }.take(200)}"
    ) {
        override val isRetryable: Boolean by lazy {
            val output = stderr.ifBlank { stdout }.lowercase()
            RETRYABLE_PATTERNS.any { pattern -> pattern in output }
        }

        companion object {
            private val RETRYABLE_PATTERNS = listOf(
                "connection refused",
                "connection reset",
                "connection timed out",
                "network is unreachable",
                "temporary failure",
                "too many requests",
                "rate limit",
                "503 service unavailable",
                "502 bad gateway",
                "504 gateway timeout",
                "i/o timeout",
                "dial tcp",
                "no such host",
                "port is already allocated"
            )
        }
    }

    /**
     * A Docker command timed out.
     * Timeouts are retryable as the operation may succeed with more time or less load.
     */
    class Timeout(
        val duration: Duration
    ) : DockerException("Command timed out after $duration") {
        override val isRetryable: Boolean = true
    }

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
