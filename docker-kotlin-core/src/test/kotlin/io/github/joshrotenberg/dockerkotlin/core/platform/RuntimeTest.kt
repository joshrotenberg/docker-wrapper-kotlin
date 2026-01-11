package io.github.joshrotenberg.dockerkotlin.core.platform

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RuntimeTest {

    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return runCatching {
                val process = ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor() == 0
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
        assertTrue(Runtime.entries.contains(runtime))
    }

    @Test
    @EnabledIf("isDockerAvailable")
    fun `detected runtime command exists`() {
        val runtime = Runtime.detect()
        val result = runCatching {
            ProcessBuilder(runtime.command, "--version")
                .redirectErrorStream(true)
                .start()
                .waitFor()
        }
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `Podman isPodman returns true`() {
        assertTrue(Runtime.PODMAN.isPodman)
    }

    @Test
    fun `Docker runtimes isPodman returns false`() {
        assertFalse(Runtime.DOCKER.isPodman)
        assertFalse(Runtime.DOCKER_DESKTOP.isPodman)
        assertFalse(Runtime.COLIMA.isPodman)
        assertFalse(Runtime.ORBSTACK.isPodman)
        assertFalse(Runtime.RANCHER_DESKTOP.isPodman)
    }

    @Test
    fun `Podman isDocker returns false`() {
        assertFalse(Runtime.PODMAN.isDocker)
    }

    @Test
    fun `Docker runtimes isDocker returns true`() {
        assertTrue(Runtime.DOCKER.isDocker)
        assertTrue(Runtime.DOCKER_DESKTOP.isDocker)
        assertTrue(Runtime.COLIMA.isDocker)
        assertTrue(Runtime.ORBSTACK.isDocker)
        assertTrue(Runtime.RANCHER_DESKTOP.isDocker)
    }

    @Test
    fun `Podman does not support builder instance commands`() {
        assertFalse(Runtime.PODMAN.supportsBuilderInstances)
        assertFalse(Runtime.PODMAN.supportsBuilderCommand("create"))
        assertFalse(Runtime.PODMAN.supportsBuilderCommand("ls"))
        assertFalse(Runtime.PODMAN.supportsBuilderCommand("rm"))
        assertFalse(Runtime.PODMAN.supportsBuilderCommand("use"))
        assertFalse(Runtime.PODMAN.supportsBuilderCommand("stop"))
    }

    @Test
    fun `Podman supports basic builder commands`() {
        assertTrue(Runtime.PODMAN.supportsBuilderCommand("build"))
        assertTrue(Runtime.PODMAN.supportsBuilderCommand("inspect"))
        assertTrue(Runtime.PODMAN.supportsBuilderCommand("prune"))
        assertTrue(Runtime.PODMAN.supportsBuilderCommand("version"))
    }

    @Test
    fun `Docker supports all builder commands`() {
        assertTrue(Runtime.DOCKER.supportsBuilderInstances)
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("create"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("ls"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("rm"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("use"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("stop"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("build"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("inspect"))
        assertTrue(Runtime.DOCKER.supportsBuilderCommand("prune"))
    }

    @Test
    fun `Docker Desktop supports all builder commands`() {
        assertTrue(Runtime.DOCKER_DESKTOP.supportsBuilderInstances)
        assertTrue(Runtime.DOCKER_DESKTOP.supportsBuilderCommand("create"))
        assertTrue(Runtime.DOCKER_DESKTOP.supportsBuilderCommand("ls"))
    }

    @Test
    fun `Podman does not have Docker compatible output`() {
        assertFalse(Runtime.PODMAN.dockerCompatibleOutput)
    }

    @Test
    fun `Docker runtimes have Docker compatible output`() {
        assertTrue(Runtime.DOCKER.dockerCompatibleOutput)
        assertTrue(Runtime.DOCKER_DESKTOP.dockerCompatibleOutput)
        assertTrue(Runtime.COLIMA.dockerCompatibleOutput)
        assertTrue(Runtime.ORBSTACK.dockerCompatibleOutput)
        assertTrue(Runtime.RANCHER_DESKTOP.dockerCompatibleOutput)
    }
}
