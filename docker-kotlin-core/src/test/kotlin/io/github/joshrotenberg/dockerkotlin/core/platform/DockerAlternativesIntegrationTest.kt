package io.github.joshrotenberg.dockerkotlin.core.platform

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.PullCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.core.command.network.NetworkCreateCommand
import io.github.joshrotenberg.dockerkotlin.core.command.network.NetworkRmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.volume.VolumeCreateCommand
import io.github.joshrotenberg.dockerkotlin.core.command.volume.VolumeRmCommand
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests that verify compatibility with Docker-compatible runtimes.
 *
 * These tests run actual Docker commands against whatever runtime is configured.
 * They are tagged with "integration" and only run when Docker is available.
 *
 * Compatible runtimes (all use `docker` CLI or alias):
 * - Docker Engine / Docker Desktop
 * - Colima (macOS) - uses docker CLI
 * - OrbStack (macOS) - uses docker CLI
 * - Rancher Desktop - uses docker CLI
 * - Podman - can be aliased as `docker` per Podman docs
 *
 * Run with: ./gradlew :docker-kotlin-core:test --tests "*IntegrationTest"
 */
@Tag("integration")
@EnabledIf("isDockerAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DockerAlternativesIntegrationTest {

    private lateinit var platformInfo: PlatformInfo
    private lateinit var executor: CommandExecutor

    companion object {
        private const val TEST_IMAGE = "alpine:latest"
        private const val TEST_CONTAINER = "docker-kotlin-integration-test"
        private const val TEST_NETWORK = "docker-kotlin-integration-network"
        private const val TEST_VOLUME = "docker-kotlin-integration-volume"

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

    @BeforeAll
    fun setup() {
        platformInfo = PlatformInfo.detect()
        executor = CommandExecutor(platformInfo)

        println("=".repeat(60))
        println("Docker Alternatives Integration Test")
        println("=".repeat(60))
        println("Runtime: ${platformInfo.runtime}")
        println("Version: ${platformInfo.version}")
        println("Platform: ${platformInfo.platform}")
        println("Socket: ${platformInfo.socketPath}")
        println("=".repeat(60))

        // Clean up any leftover resources from previous runs
        cleanup()
    }

    @AfterAll
    fun teardown() {
        cleanup()
    }

    private fun cleanup() {
        listOf(
            listOf("rm", "-f", TEST_CONTAINER),
            listOf("network", "rm", TEST_NETWORK),
            listOf("volume", "rm", TEST_VOLUME)
        ).forEach { cmd ->
            runCatching { runBlocking { executor.execute(cmd) } }
        }
    }

    @Test
    @Order(1)
    fun `pull image`() = runBlocking {
        val command = PullCommand(TEST_IMAGE, executor)
        command.execute()
        // If we get here without exception, pull succeeded
    }

    @Test
    @Order(2)
    fun `create and remove network`() = runBlocking {
        // Create network
        val networkId = NetworkCreateCommand(TEST_NETWORK, executor).execute()
        assertTrue(networkId.isNotBlank(), "Network create should return network ID")

        // Remove network
        NetworkRmCommand(TEST_NETWORK, executor).execute()
    }

    @Test
    @Order(3)
    fun `create and remove volume`() = runBlocking {
        // Create volume
        val volumeName = VolumeCreateCommand(executor).name(TEST_VOLUME).execute()
        assertEquals(TEST_VOLUME, volumeName, "Volume create should return volume name")

        // Remove volume
        VolumeRmCommand(TEST_VOLUME, executor).execute()
    }

    @Test
    @Order(4)
    fun `run container and capture output`() = runBlocking {
        val containerId = RunCommand(TEST_IMAGE, executor)
            .name(TEST_CONTAINER)
            .rm()
            .command("echo", "hello from docker-kotlin")
            .execute()

        assertTrue(containerId.value.isNotBlank(), "Should return container ID or output")
    }

    @Test
    @Order(5)
    fun `run detached container and remove`() = runBlocking {
        // Run detached
        val containerId = RunCommand(TEST_IMAGE, executor)
            .name(TEST_CONTAINER)
            .detach()
            .command("sleep", "30")
            .execute()

        assertTrue(containerId.value.isNotBlank(), "Should return container ID")

        // Remove with force
        RmCommand(TEST_CONTAINER, executor).force().execute()
    }

    @Test
    @Order(6)
    fun `docker info diagnostic`() = runBlocking {
        // Diagnostic test to understand CI behavior
        val output = try {
            executor.execute(listOf("info"))
        } catch (e: Exception) {
            println("=== DOCKER INFO EXCEPTION ===")
            println("Exception type: ${e::class.simpleName}")
            println("Message: ${e.message}")
            throw e
        }

        println("=== DOCKER INFO OUTPUT ===")
        println("Exit code: ${output.exitCode}")
        println("Stdout length: ${output.stdout.length}")
        println("Stderr length: ${output.stderr.length}")
        println("--- STDOUT (first 500 chars) ---")
        println(output.stdout.take(500))
        println("--- STDERR ---")
        println(output.stderr)
        println("=== END DOCKER INFO ===")

        assertEquals(0, output.exitCode, "docker info should succeed: ${output.stderr}")
    }

    @Test
    @Order(7)
    fun `docker version works`() = runBlocking {
        val output = executor.execute(listOf("version"))
        assertEquals(0, output.exitCode, "docker version should succeed")
        assertTrue(
            output.stdout.contains("Client") || output.stdout.contains("Version"),
            "Should return docker version output"
        )
    }
}
