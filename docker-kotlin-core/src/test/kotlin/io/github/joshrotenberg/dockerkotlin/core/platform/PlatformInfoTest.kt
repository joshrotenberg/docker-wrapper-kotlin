package io.github.joshrotenberg.dockerkotlin.core.platform

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlatformInfoTest {

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
    @EnabledIf("isDockerAvailable")
    fun `detect returns valid platform info`() {
        val info = PlatformInfo.detect()

        assertNotNull(info.runtime)
        assertNotNull(info.platform)
        assertNotEquals("unknown", info.version, "Should detect Docker version")
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
    fun `socket path is detected on unix systems`() {
        val info = PlatformInfo.detect()

        if (info.platform != Platform.WINDOWS) {
            // Socket path should be set on Unix systems
            // May be null if DOCKER_HOST uses tcp:// or other protocol
            val dockerHost = System.getenv("DOCKER_HOST")
            if (dockerHost == null || dockerHost.startsWith("unix://")) {
                assertNotNull(info.socketPath, "Socket path should be detected on Unix")
                assertTrue(
                    info.socketPath.toString().endsWith(".sock"),
                    "Socket path should end with .sock"
                )
            }
        }
    }

    @Test
    @EnabledIf("isDockerAvailable")
    fun `version is parseable`() {
        val info = PlatformInfo.detect()

        // Version should be something like "24.0.7" or "4.25.0"
        assertTrue(
            info.version.matches(Regex("^\\d+\\..*")) || info.version == "unknown",
            "Version '${info.version}' should start with a number"
        )
    }

    @Test
    @Tag("integration")
    @EnabledIf("isDockerAvailable")
    fun `runtime command is executable`() {
        val info = PlatformInfo.detect()

        val result = runCatching {
            ProcessBuilder(info.runtime.command, "info", "--format", "{{.ID}}")
                .redirectErrorStream(true)
                .start()
                .waitFor()
        }

        assertTrue(result.isSuccess, "Should be able to execute docker info")
        assertEquals(0, result.getOrNull(), "docker info should succeed")
    }
}
