package io.github.joshrotenberg.dockerkotlin.core.command

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for container commands.
 *
 * These tests require Docker to be running and will create/destroy real containers.
 * Set DOCKER_TESTS_ENABLED=true to run these tests.
 */
@EnabledIfEnvironmentVariable(named = "DOCKER_TESTS_ENABLED", matches = "true")
class ContainerCommandsIntegrationTest {

    private val testContainerPrefix = "docker-kotlin-test-"
    private val createdContainers = mutableListOf<String>()

    @BeforeEach
    fun setup() {
        // Pull alpine image if not present
        runBlocking {
            try {
                PullCommand("alpine:latest").execute()
            } catch (e: Exception) {
                // Image might already exist
            }
        }
    }

    @AfterEach
    fun cleanup() {
        // Clean up any containers created during tests
        createdContainers.forEach { name ->
            try {
                StopCommand(name).executeBlocking()
            } catch (e: Exception) {
                // Container might already be stopped
            }
            try {
                RmCommand(name).force().executeBlocking()
            } catch (e: Exception) {
                // Container might already be removed
            }
        }
        createdContainers.clear()
    }

    private fun uniqueContainerName(): String {
        val name = "$testContainerPrefix${UUID.randomUUID().toString().take(8)}"
        createdContainers.add(name)
        return name
    }

    @Test
    fun `run and stop container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        val containerId = RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        assertNotNull(containerId)
        assertTrue(containerId.value.isNotBlank())

        // Stop the container
        StopCommand(name).time(1).execute()
    }

    @Test
    fun `create start and stop container`() = runBlocking {
        val name = uniqueContainerName()

        // Create container without starting
        val containerId = CreateCommand("alpine:latest")
            .name(name)
            .command("sleep", "300")
            .execute()

        assertNotNull(containerId)

        // Start the container
        StartCommand(name).execute()

        // Verify it's running via ps
        val containers = PsCommand().filterName(name).execute()
        assertTrue(containers.isNotEmpty() || PsCommand().filterName(name).quiet().executeIds().isNotEmpty())

        // Stop it
        StopCommand(name).time(1).execute()
    }

    @Test
    fun `exec command in container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Execute a command
        val output = ExecCommand(name, "echo", "hello")
            .execute()

        assertEquals("hello", output.stdout.trim())
        assertTrue(output.success)
    }

    @Test
    fun `pause and unpause container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Pause
        PauseCommand(name).execute()

        // Unpause
        UnpauseCommand(name).execute()

        // Stop
        StopCommand(name).time(1).execute()
    }

    @Test
    fun `rename container`() = runBlocking {
        val originalName = uniqueContainerName()
        val newName = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(originalName)
            .detach()
            .command("sleep", "300")
            .execute()

        // Rename it
        RenameCommand(originalName, newName).execute()

        // Remove original from cleanup list, add new name
        createdContainers.remove(originalName)

        // Stop with new name
        StopCommand(newName).time(1).execute()
    }

    @Test
    fun `inspect container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Inspect it
        val json = InspectCommand(name).execute()
        assertTrue(json.contains(name))
        assertTrue(json.contains("alpine"))

        // Get specific field
        val state = InspectCommand(name)
            .format("{{.State.Running}}")
            .execute()
        assertEquals("true", state.trim())

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `logs from container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container that outputs something
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sh", "-c", "echo 'test output' && sleep 300")
            .execute()

        // Give it a moment to start
        Thread.sleep(500)

        // Get logs
        val logs = LogsCommand(name).execute()
        assertTrue(logs.contains("test output"))

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `top shows processes`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Get process list
        val top = TopCommand(name).execute()
        assertTrue(top.contains("sleep") || top.contains("PID"))

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `diff shows filesystem changes`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container and make a change
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sh", "-c", "touch /newfile && sleep 300")
            .execute()

        // Give it a moment
        Thread.sleep(500)

        // Get diff
        val changes = DiffCommand(name).execute()
        assertTrue(changes.any { it.path.contains("newfile") })

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `restart container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Restart it
        RestartCommand(name).time(1).execute()

        // Verify still running
        val running = InspectCommand(name)
            .format("{{.State.Running}}")
            .execute()
        assertEquals("true", running.trim())

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `kill container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Kill it
        KillCommand(name).execute()

        // Verify not running
        val running = InspectCommand(name)
            .format("{{.State.Running}}")
            .execute()
        assertEquals("false", running.trim())
    }

    @Test
    fun `wait for container`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container that exits quickly
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sh", "-c", "exit 42")
            .execute()

        // Wait for it
        val exitCodes = WaitCommand(name).execute()
        assertEquals(listOf(42), exitCodes)
    }

    @Test
    fun `ps lists containers`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // List containers
        val ids = PsCommand().filterName(name).executeIds()
        assertTrue(ids.isNotEmpty())

        StopCommand(name).time(1).execute()
    }

    @Test
    fun `stats shows resource usage`() = runBlocking {
        val name = uniqueContainerName()

        // Run a container
        RunCommand("alpine:latest")
            .name(name)
            .detach()
            .command("sleep", "300")
            .execute()

        // Get stats (no-stream for single snapshot)
        val stats = StatsCommand(name).noStream().execute()
        assertTrue(stats.contains(name) || stats.contains("CONTAINER"))

        StopCommand(name).time(1).execute()
    }
}
