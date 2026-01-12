package io.github.joshrotenberg.dockerkotlin.core.retry

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class RetryExecutorTest {

    @Test
    fun `executeWithRetry succeeds on first attempt`() = runTest {
        var attempts = 0
        val result = RetryExecutor.executeWithRetry(RetryPolicy.DEFAULT) {
            attempts++
            "success"
        }

        assertEquals("success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `executeWithRetry retries on retryable exception`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds)
        )

        val result = RetryExecutor.executeWithRetry(policy) {
            attempts++
            if (attempts < 3) {
                throw DockerException.Timeout(30.milliseconds)
            }
            "success"
        }

        assertEquals("success", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `executeWithRetry throws after max attempts`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds)
        )

        assertThrows<DockerException.Timeout> {
            RetryExecutor.executeWithRetry(policy) {
                attempts++
                throw DockerException.Timeout(30.milliseconds)
            }
        }

        assertEquals(3, attempts)
    }

    @Test
    fun `executeWithRetry does not retry non-retryable exceptions`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds)
        )

        assertThrows<DockerException.DockerNotFound> {
            RetryExecutor.executeWithRetry(policy) {
                attempts++
                throw DockerException.DockerNotFound()
            }
        }

        assertEquals(1, attempts)
    }

    @Test
    fun `executeWithRetryBlocking succeeds on first attempt`() {
        var attempts = 0
        val result = RetryExecutor.executeWithRetryBlocking(RetryPolicy.DEFAULT) {
            attempts++
            "success"
        }

        assertEquals("success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `executeWithRetryBlocking retries on retryable exception`() {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds)
        )

        val result = RetryExecutor.executeWithRetryBlocking(policy) {
            attempts++
            if (attempts < 3) {
                throw DockerException.Timeout(30.milliseconds)
            }
            "success"
        }

        assertEquals("success", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `executeWithRetryBlocking throws after max attempts`() {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds)
        )

        assertThrows<DockerException.Timeout> {
            RetryExecutor.executeWithRetryBlocking(policy) {
                attempts++
                throw DockerException.Timeout(30.milliseconds)
            }
        }

        assertEquals(3, attempts)
    }

    @Test
    fun `custom retryOn predicate is used`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            backoff = BackoffStrategy.Fixed(1.milliseconds),
            retryOn = { false } // Never retry
        )

        assertThrows<DockerException.Timeout> {
            RetryExecutor.executeWithRetry(policy) {
                attempts++
                throw DockerException.Timeout(30.milliseconds)
            }
        }

        assertEquals(1, attempts) // Only one attempt since retryOn returns false
    }
}
