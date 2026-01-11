package io.github.joshrotenberg.dockerkotlin.core.platform

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for PlatformInfo detection.
 *
 * Tests that require a running Docker daemon are in DockerAlternativesIntegrationTest.
 */
class PlatformInfoTest {

    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            // Check if Docker daemon is running, not just if CLI exists
            return runCatching {
                val process = ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor() == 0
            }.getOrDefault(false)
        }
    }

    @Test
    fun `detected platform matches OS`() {
        val info = PlatformInfo.detect()
        val osName = System.getProperty("os.name").lowercase()

        when {
            osName.contains("mac") -> assertEquals(Platform.MACOS, info.platform)
            osName.contains("linux") -> assertEquals(Platform.LINUX, info.platform)
            osName.contains("win") -> assertEquals(Platform.WINDOWS, info.platform)
        }
    }

    @Test
    @EnabledIf("isDockerAvailable")
    fun `detect returns valid platform info`() {
        val info = PlatformInfo.detect()

        assertNotNull(info.runtime)
        assertNotNull(info.platform)
        // Version may be "unknown" if daemon is unavailable
    }

    @Test
    @EnabledIf("isDockerAvailable")
    fun `version is parseable when docker available`() {
        val info = PlatformInfo.detect()

        // Version should be something like "24.0.7" or "4.25.0" or "unknown"
        assertTrue(
            info.version.matches(Regex("^\\d+\\..*")) || info.version == "unknown",
            "Version '${info.version}' should start with a number or be 'unknown'"
        )
    }
}
