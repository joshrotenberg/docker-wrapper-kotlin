package io.github.joshrotenberg.dockerkotlin.core.retry

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class BackoffStrategyTest {

    @Test
    fun `Fixed returns constant delay`() {
        val strategy = BackoffStrategy.Fixed(500.milliseconds)

        assertEquals(500.milliseconds, strategy.delayFor(1))
        assertEquals(500.milliseconds, strategy.delayFor(2))
        assertEquals(500.milliseconds, strategy.delayFor(5))
    }

    @Test
    fun `Linear increases delay linearly`() {
        val strategy = BackoffStrategy.Linear(
            initial = 100.milliseconds,
            increment = 50.milliseconds
        )

        assertEquals(100.milliseconds, strategy.delayFor(1))
        assertEquals(150.milliseconds, strategy.delayFor(2))
        assertEquals(200.milliseconds, strategy.delayFor(3))
        assertEquals(250.milliseconds, strategy.delayFor(4))
    }

    @Test
    fun `Exponential increases delay exponentially`() {
        val strategy = BackoffStrategy.Exponential(
            initial = 100.milliseconds,
            max = 10.seconds,
            multiplier = 2.0
        )

        assertEquals(100.milliseconds, strategy.delayFor(1))
        assertEquals(200.milliseconds, strategy.delayFor(2))
        assertEquals(400.milliseconds, strategy.delayFor(3))
        assertEquals(800.milliseconds, strategy.delayFor(4))
    }

    @Test
    fun `Exponential respects max delay`() {
        val strategy = BackoffStrategy.Exponential(
            initial = 1.seconds,
            max = 5.seconds,
            multiplier = 2.0
        )

        assertEquals(1.seconds, strategy.delayFor(1))
        assertEquals(2.seconds, strategy.delayFor(2))
        assertEquals(4.seconds, strategy.delayFor(3))
        assertEquals(5.seconds, strategy.delayFor(4)) // Capped at max
        assertEquals(5.seconds, strategy.delayFor(5)) // Still capped
    }

    @Test
    fun `Exponential with custom multiplier`() {
        val strategy = BackoffStrategy.Exponential(
            initial = 100.milliseconds,
            max = 10.seconds,
            multiplier = 3.0
        )

        assertEquals(100.milliseconds, strategy.delayFor(1))
        assertEquals(300.milliseconds, strategy.delayFor(2))
        assertEquals(900.milliseconds, strategy.delayFor(3))
    }

    @Test
    fun `companion factory methods work`() {
        val fixed = BackoffStrategy.fixed(1.seconds)
        assertTrue(fixed is BackoffStrategy.Fixed)

        val linear = BackoffStrategy.linear(100.milliseconds, 50.milliseconds)
        assertTrue(linear is BackoffStrategy.Linear)

        val exponential = BackoffStrategy.exponential()
        assertTrue(exponential is BackoffStrategy.Exponential)
    }
}
