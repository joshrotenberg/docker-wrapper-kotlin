package io.github.joshrotenberg.dockerkotlin.core.platform

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RuntimeTest {

    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return runCatching {
                ProcessBuilder("docker", "version")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor() == 0
            }.getOrDefault(false)
        }
    }

    @Test
    fun `Runtime enum has expected values`() {
        val runtimes = Runtime.entries
        assertTrue(runtimes.contains(Runtime.DOCKER))
        assertTrue(runtimes.contains(Runtime.PODMAN))
        assertTrue(runtimes.contains(Runtime.COLIMA))
        assertTrue(runtimes.contains(Runtime.ORBSTACK))
        assertTrue(runtimes.contains(Runtime.RANCHER_DESKTOP))
        assertTrue(runtimes.contains(Runtime.DOCKER_DESKTOP))
    }

    @Test
    fun `Runtime commands are correct`() {
        assertEquals("docker", Runtime.DOCKER.command)
        assertEquals("podman", Runtime.PODMAN.command)
        assertEquals("docker", Runtime.COLIMA.command)
        assertEquals("docker", Runtime.ORBSTACK.command)
        assertEquals("docker", Runtime.RANCHER_DESKTOP.command)
        assertEquals("docker", Runtime.DOCKER_DESKTOP.command)
    }

    @Test
    @EnabledIf("isDockerAvailable")
    fun `detect returns a valid runtime`() {
        val runtime = Runtime.detect()
        assertNotNull(runtime)
        assertTrue(runtime in Runtime.entries)
    }

    @Test
    @Tag("integration")
    @EnabledIf("isDockerAvailable")
    fun `detected runtime command exists`() {
        val runtime = Runtime.detect()
        val result = runCatching {
            ProcessBuilder(runtime.command, "version")
                .redirectErrorStream(true)
                .start()
                .waitFor()
        }
        assertTrue(result.isSuccess, "Runtime command '${runtime.command}' should be executable")
        assertEquals(0, result.getOrNull(), "Runtime command should exit with 0")
    }
}
