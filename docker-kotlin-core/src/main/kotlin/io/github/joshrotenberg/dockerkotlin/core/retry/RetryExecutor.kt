package io.github.joshrotenberg.dockerkotlin.core.retry

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

/**
 * Executes operations with retry support.
 */
object RetryExecutor {
    private val logger = LoggerFactory.getLogger(RetryExecutor::class.java)

    /**
     * Execute an operation with retry support (suspend version).
     *
     * @param policy The retry policy to use
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws DockerException if all retries are exhausted
     */
    suspend fun <T> executeWithRetry(
        policy: RetryPolicy,
        operation: suspend () -> T
    ): T {
        var lastException: DockerException? = null
        var attempt = 0

        while (attempt < policy.maxAttempts) {
            attempt++
            try {
                return operation()
            } catch (e: DockerException) {
                lastException = e

                if (attempt >= policy.maxAttempts) {
                    logger.debug("All {} attempts exhausted", policy.maxAttempts)
                    throw e
                }

                if (!policy.retryOn(e)) {
                    logger.debug("Exception is not retryable: {}", e.message)
                    throw e
                }

                val delayDuration = policy.backoff.delayFor(attempt)
                logger.debug(
                    "Attempt {} failed, retrying in {}: {}",
                    attempt,
                    delayDuration,
                    e.message
                )
                delay(delayDuration.inWholeMilliseconds)
            }
        }

        throw lastException ?: DockerException.Generic("Retry failed without exception")
    }

    /**
     * Execute an operation with retry support (blocking version).
     *
     * @param policy The retry policy to use
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws DockerException if all retries are exhausted
     */
    fun <T> executeWithRetryBlocking(
        policy: RetryPolicy,
        operation: () -> T
    ): T {
        var lastException: DockerException? = null
        var attempt = 0

        while (attempt < policy.maxAttempts) {
            attempt++
            try {
                return operation()
            } catch (e: DockerException) {
                lastException = e

                if (attempt >= policy.maxAttempts) {
                    logger.debug("All {} attempts exhausted", policy.maxAttempts)
                    throw e
                }

                if (!policy.retryOn(e)) {
                    logger.debug("Exception is not retryable: {}", e.message)
                    throw e
                }

                val delayDuration = policy.backoff.delayFor(attempt)
                logger.debug(
                    "Attempt {} failed, retrying in {}: {}",
                    attempt,
                    delayDuration,
                    e.message
                )
                Thread.sleep(delayDuration.inWholeMilliseconds)
            }
        }

        throw lastException ?: DockerException.Generic("Retry failed without exception")
    }
}
