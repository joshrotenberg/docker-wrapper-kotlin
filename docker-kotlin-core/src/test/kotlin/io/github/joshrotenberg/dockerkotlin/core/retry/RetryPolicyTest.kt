package io.github.joshrotenberg.dockerkotlin.core.retry

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {

    @Test
    fun `default policy has 3 max attempts`() {
        val policy = RetryPolicy.DEFAULT
        assertEquals(3, policy.maxAttempts)
    }

    @Test
    fun `default policy uses exponential backoff`() {
        val policy = RetryPolicy.DEFAULT
        assertTrue(policy.backoff is BackoffStrategy.Exponential)
    }

    @Test
    fun `NONE policy has 1 max attempt`() {
        val policy = RetryPolicy.NONE
        assertEquals(1, policy.maxAttempts)
    }

    @Test
    fun `maxAttempts must be at least 1`() {
        assertThrows<IllegalArgumentException> {
            RetryPolicy(maxAttempts = 0)
        }
    }

    @Test
    fun `default retryOn uses isRetryable property`() {
        val policy = RetryPolicy.DEFAULT

        val retryableException = DockerException.Timeout(30.seconds)
        assertTrue(policy.retryOn(retryableException))

        val nonRetryableException = DockerException.DockerNotFound()
        assertFalse(policy.retryOn(nonRetryableException))
    }

    @Test
    fun `builder creates policy with custom settings`() {
        val policy = RetryPolicy.builder()
            .maxAttempts(5)
            .backoff(BackoffStrategy.Fixed(1.seconds))
            .build()

        assertEquals(5, policy.maxAttempts)
        assertTrue(policy.backoff is BackoffStrategy.Fixed)
    }

    @Test
    fun `builder retryOnAll always returns true`() {
        val policy = RetryPolicy.builder()
            .retryOnAll()
            .build()

        assertTrue(policy.retryOn(DockerException.DockerNotFound()))
        assertTrue(policy.retryOn(DockerException.Timeout(30.seconds)))
    }

    @Test
    fun `builder retryOnTimeout only retries timeouts`() {
        val policy = RetryPolicy.builder()
            .retryOnTimeout()
            .build()

        assertTrue(policy.retryOn(DockerException.Timeout(30.seconds)))
        assertFalse(policy.retryOn(DockerException.DockerNotFound()))
    }
}
