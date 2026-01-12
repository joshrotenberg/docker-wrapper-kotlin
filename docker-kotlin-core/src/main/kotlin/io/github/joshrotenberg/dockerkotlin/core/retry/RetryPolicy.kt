package io.github.joshrotenberg.dockerkotlin.core.retry

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Backoff strategy for retry delays.
 */
sealed class BackoffStrategy {
    /**
     * Calculate the delay for the given attempt number.
     *
     * @param attempt The attempt number (1-based, where 1 is the first retry)
     * @return The delay before this attempt
     */
    abstract fun delayFor(attempt: Int): Duration

    /**
     * Fixed delay between retries.
     *
     * @param delay The constant delay between attempts
     */
    data class Fixed(val delay: Duration = 1.seconds) : BackoffStrategy() {
        override fun delayFor(attempt: Int): Duration = delay
    }

    /**
     * Linear backoff with increasing delays.
     *
     * @param initial The initial delay
     * @param increment The amount to add for each subsequent attempt
     */
    data class Linear(
        val initial: Duration = 100.milliseconds,
        val increment: Duration = 100.milliseconds
    ) : BackoffStrategy() {
        override fun delayFor(attempt: Int): Duration =
            initial + (increment * (attempt - 1))
    }

    /**
     * Exponential backoff with multiplicative increase.
     *
     * @param initial The initial delay
     * @param max The maximum delay (cap)
     * @param multiplier The multiplier for each subsequent attempt
     */
    data class Exponential(
        val initial: Duration = 100.milliseconds,
        val max: Duration = 10.seconds,
        val multiplier: Double = 2.0
    ) : BackoffStrategy() {
        override fun delayFor(attempt: Int): Duration {
            val factor = pow(multiplier, attempt - 1)
            val delay = initial * factor
            return if (delay > max) max else delay
        }

        private fun pow(base: Double, n: Int): Double {
            var result = 1.0
            repeat(n) { result *= base }
            return result
        }
    }

    companion object {
        /**
         * Create a fixed delay backoff strategy.
         */
        @JvmStatic
        fun fixed(delay: Duration = 1.seconds): BackoffStrategy = Fixed(delay)

        /**
         * Create a linear backoff strategy.
         */
        @JvmStatic
        fun linear(
            initial: Duration = 100.milliseconds,
            increment: Duration = 100.milliseconds
        ): BackoffStrategy = Linear(initial, increment)

        /**
         * Create an exponential backoff strategy.
         */
        @JvmStatic
        fun exponential(
            initial: Duration = 100.milliseconds,
            max: Duration = 10.seconds,
            multiplier: Double = 2.0
        ): BackoffStrategy = Exponential(initial, max, multiplier)
    }
}

/**
 * Configuration for retry behavior.
 *
 * @param maxAttempts Maximum number of attempts (including the initial attempt)
 * @param backoff The backoff strategy to use between retries
 * @param retryOn Predicate to determine if an exception should trigger a retry
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val backoff: BackoffStrategy = BackoffStrategy.Exponential(),
    val retryOn: (DockerException) -> Boolean = { it.isRetryable }
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }
    }

    companion object {
        /**
         * Default retry policy with exponential backoff.
         */
        @JvmField
        val DEFAULT = RetryPolicy()

        /**
         * No retries - fail immediately.
         */
        @JvmField
        val NONE = RetryPolicy(maxAttempts = 1)

        /**
         * Create a retry policy builder (for Java interop).
         */
        @JvmStatic
        fun builder(): RetryPolicyBuilder = RetryPolicyBuilder()
    }
}

/**
 * Builder for RetryPolicy (Java-friendly).
 */
class RetryPolicyBuilder {
    private var maxAttempts: Int = 3
    private var backoff: BackoffStrategy = BackoffStrategy.Exponential()
    private var retryOn: (DockerException) -> Boolean = { it.isRetryable }

    fun maxAttempts(attempts: Int) = apply { maxAttempts = attempts }
    fun backoff(strategy: BackoffStrategy) = apply { backoff = strategy }
    fun retryOn(predicate: (DockerException) -> Boolean) = apply { retryOn = predicate }

    /**
     * Retry on all exceptions.
     */
    fun retryOnAll() = apply { retryOn = { true } }

    /**
     * Retry only on timeout exceptions.
     */
    fun retryOnTimeout() = apply { retryOn = { it is DockerException.Timeout } }

    fun build(): RetryPolicy = RetryPolicy(maxAttempts, backoff, retryOn)
}
