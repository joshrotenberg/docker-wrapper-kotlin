package io.github.joshrotenberg.dockerkotlin.core.error

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class DockerExceptionTest {

    @Test
    fun `DockerNotFound is not retryable`() {
        val exception = DockerException.DockerNotFound()
        assertFalse(exception.isRetryable)
    }

    @Test
    fun `DaemonNotRunning is retryable`() {
        val exception = DockerException.DaemonNotRunning()
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `Timeout is retryable`() {
        val exception = DockerException.Timeout(30.seconds)
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `ContainerNotFound is not retryable`() {
        val exception = DockerException.ContainerNotFound("abc123")
        assertFalse(exception.isRetryable)
    }

    @Test
    fun `ImageNotFound is not retryable`() {
        val exception = DockerException.ImageNotFound("nginx:latest")
        assertFalse(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with connection error is retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker pull nginx",
            exitCode = 1,
            stdout = "",
            stderr = "Error: connection refused"
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with rate limit is retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker pull nginx",
            exitCode = 1,
            stdout = "",
            stderr = "toomanyrequests: Rate limit exceeded"
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with 503 is retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker pull nginx",
            exitCode = 1,
            stdout = "",
            stderr = "503 Service Unavailable"
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with port conflict is retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker run -p 8080:80 nginx",
            exitCode = 1,
            stdout = "",
            stderr = "port is already allocated"
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with general error is not retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker run nginx",
            exitCode = 1,
            stdout = "",
            stderr = "invalid reference format"
        )
        assertFalse(exception.isRetryable)
    }

    @Test
    fun `CommandFailed with network unreachable is retryable`() {
        val exception = DockerException.CommandFailed(
            command = "docker pull nginx",
            exitCode = 1,
            stdout = "",
            stderr = "network is unreachable"
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `CommandFailed checks stdout if stderr is empty`() {
        val exception = DockerException.CommandFailed(
            command = "docker pull nginx",
            exitCode = 1,
            stdout = "Error: connection timed out",
            stderr = ""
        )
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `Generic exception is not retryable by default`() {
        val exception = DockerException.Generic("Something went wrong")
        assertFalse(exception.isRetryable)
    }
}
