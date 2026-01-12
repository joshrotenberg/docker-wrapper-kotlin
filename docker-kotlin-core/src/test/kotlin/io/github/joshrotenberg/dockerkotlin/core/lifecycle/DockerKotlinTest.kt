package io.github.joshrotenberg.dockerkotlin.core.lifecycle

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class DockerKotlinTest {

    @BeforeEach
    fun setup() {
        DockerKotlin.reset()
    }

    @AfterEach
    fun teardown() {
        DockerKotlin.reset()
    }

    @Test
    fun `sessionId is generated and consistent`() {
        val sessionId = DockerKotlin.sessionId
        assertNotNull(sessionId)
        assertEquals(8, sessionId.length)
        assertEquals(sessionId, DockerKotlin.sessionId) // Same across calls
    }

    @Test
    fun `managedLabels contains required labels`() {
        val labels = DockerKotlin.managedLabels()

        assertTrue(labels.containsKey(DockerKotlin.LABEL_MANAGED))
        assertTrue(labels.containsKey(DockerKotlin.LABEL_SESSION))
        assertTrue(labels.containsKey(DockerKotlin.LABEL_CREATED))

        assertEquals("true", labels[DockerKotlin.LABEL_MANAGED])
        assertEquals(DockerKotlin.sessionId, labels[DockerKotlin.LABEL_SESSION])
    }

    @Test
    fun `track adds container to tracked set`() {
        assertTrue(DockerKotlin.trackedContainers().isEmpty())

        DockerKotlin.track("container1")
        DockerKotlin.track("container2")

        val tracked = DockerKotlin.trackedContainers()
        assertEquals(2, tracked.size)
        assertTrue("container1" in tracked)
        assertTrue("container2" in tracked)
    }

    @Test
    fun `untrack removes container from tracked set`() {
        DockerKotlin.track("container1")
        DockerKotlin.track("container2")

        DockerKotlin.untrack("container1")

        val tracked = DockerKotlin.trackedContainers()
        assertEquals(1, tracked.size)
        assertFalse("container1" in tracked)
        assertTrue("container2" in tracked)
    }

    @Test
    fun `configure updates settings`() {
        DockerKotlin.configure {
            enableShutdownHook = false
            shutdownTimeout = 60.seconds
            cleanupOnShutdown = false
        }

        val config = DockerKotlin.getConfig()
        assertFalse(config.enableShutdownHook)
        assertEquals(60.seconds, config.shutdownTimeout)
        assertFalse(config.cleanupOnShutdown)
    }

    @Test
    fun `default config has expected values`() {
        val config = DockerKotlin.getConfig()

        assertTrue(config.enableShutdownHook)
        assertEquals(30.seconds, config.shutdownTimeout)
        assertTrue(config.cleanupOnShutdown)
    }

    @Test
    fun `cleanupAll returns 0 when no containers tracked`() {
        val cleaned = DockerKotlin.cleanupAll()
        assertEquals(0, cleaned)
    }

    @Test
    fun `LABEL_PREFIX has correct value`() {
        assertEquals("io.github.docker-kotlin", DockerKotlin.LABEL_PREFIX)
    }

    @Test
    fun `LABEL_SESSION has correct format`() {
        assertEquals("io.github.docker-kotlin.session", DockerKotlin.LABEL_SESSION)
    }

    @Test
    fun `LABEL_MANAGED has correct format`() {
        assertEquals("io.github.docker-kotlin.managed", DockerKotlin.LABEL_MANAGED)
    }

    @Test
    fun `LABEL_CREATED has correct format`() {
        assertEquals("io.github.docker-kotlin.created", DockerKotlin.LABEL_CREATED)
    }
}
