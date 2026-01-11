package io.github.joshrotenberg.dockerkotlin.core.command.builder

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BuilderCommandsTest {

    // BuilderCreateCommand tests

    @Test
    fun `BuilderCreateCommand buildArgs basic`() {
        val command = BuilderCreateCommand()
        assertEquals(listOf("buildx", "create"), command.buildArgs())
    }

    @Test
    fun `BuilderCreateCommand buildArgs with name`() {
        val command = BuilderCreateCommand().name("my-builder")
        assertEquals(listOf("buildx", "create", "--name", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderCreateCommand buildArgs with driver`() {
        val command = BuilderCreateCommand()
            .name("my-builder")
            .driver("docker-container")
        assertEquals(
            listOf("buildx", "create", "--name", "my-builder", "--driver", "docker-container"),
            command.buildArgs()
        )
    }

    @Test
    fun `BuilderCreateCommand buildArgs with driver opts`() {
        val command = BuilderCreateCommand()
            .name("my-builder")
            .driverOpt("network", "host")
            .driverOpt("image", "moby/buildkit:latest")
        assertEquals(
            listOf(
                "buildx", "create",
                "--name", "my-builder",
                "--driver-opt", "network=host",
                "--driver-opt", "image=moby/buildkit:latest"
            ),
            command.buildArgs()
        )
    }

    @Test
    fun `BuilderCreateCommand buildArgs with platform`() {
        val command = BuilderCreateCommand()
            .name("multiarch")
            .platform("linux/amd64,linux/arm64")
        assertEquals(
            listOf("buildx", "create", "--name", "multiarch", "--platform", "linux/amd64,linux/arm64"),
            command.buildArgs()
        )
    }

    @Test
    fun `BuilderCreateCommand buildArgs with use and bootstrap`() {
        val command = BuilderCreateCommand()
            .name("my-builder")
            .use()
            .bootstrap()
        assertEquals(
            listOf("buildx", "create", "--name", "my-builder", "--use", "--bootstrap"),
            command.buildArgs()
        )
    }

    // BuilderLsCommand tests

    @Test
    fun `BuilderLsCommand buildArgs basic`() {
        val command = BuilderLsCommand()
        assertEquals(listOf("buildx", "ls"), command.buildArgs())
    }

    @Test
    fun `BuilderLsCommand buildArgs with format`() {
        val command = BuilderLsCommand().format("{{.Name}}")
        assertEquals(listOf("buildx", "ls", "--format", "{{.Name}}"), command.buildArgs())
    }

    @Test
    fun `BuilderLsCommand buildArgs json`() {
        val command = BuilderLsCommand().json()
        assertEquals(listOf("buildx", "ls", "--format", "json"), command.buildArgs())
    }

    // BuilderInspectCommand tests

    @Test
    fun `BuilderInspectCommand buildArgs basic`() {
        val command = BuilderInspectCommand()
        assertEquals(listOf("buildx", "inspect"), command.buildArgs())
    }

    @Test
    fun `BuilderInspectCommand buildArgs with name`() {
        val command = BuilderInspectCommand("my-builder")
        assertEquals(listOf("buildx", "inspect", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderInspectCommand buildArgs with bootstrap`() {
        val command = BuilderInspectCommand("my-builder").bootstrap()
        assertEquals(listOf("buildx", "inspect", "--bootstrap", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderInspectCommand buildArgs json`() {
        val command = BuilderInspectCommand().json()
        assertEquals(listOf("buildx", "inspect", "--format", "json"), command.buildArgs())
    }

    // BuilderRmCommand tests

    @Test
    fun `BuilderRmCommand buildArgs basic`() {
        val command = BuilderRmCommand("my-builder")
        assertEquals(listOf("buildx", "rm", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderRmCommand buildArgs with force`() {
        val command = BuilderRmCommand("my-builder").force()
        assertEquals(listOf("buildx", "rm", "--force", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderRmCommand buildArgs all inactive`() {
        val command = BuilderRmCommand().allInactive()
        assertEquals(listOf("buildx", "rm", "--all-inactive"), command.buildArgs())
    }

    @Test
    fun `BuilderRmCommand buildArgs with keep options`() {
        val command = BuilderRmCommand("my-builder")
            .keepState()
            .keepDaemon()
        assertEquals(
            listOf("buildx", "rm", "--keep-state", "--keep-daemon", "my-builder"),
            command.buildArgs()
        )
    }

    // BuilderUseCommand tests

    @Test
    fun `BuilderUseCommand buildArgs basic`() {
        val command = BuilderUseCommand("my-builder")
        assertEquals(listOf("buildx", "use", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderUseCommand buildArgs with default`() {
        val command = BuilderUseCommand("my-builder").default()
        assertEquals(listOf("buildx", "use", "--default", "my-builder"), command.buildArgs())
    }

    @Test
    fun `BuilderUseCommand buildArgs with global`() {
        val command = BuilderUseCommand("my-builder").global()
        assertEquals(listOf("buildx", "use", "--global", "my-builder"), command.buildArgs())
    }

    // BuilderStopCommand tests

    @Test
    fun `BuilderStopCommand buildArgs basic`() {
        val command = BuilderStopCommand()
        assertEquals(listOf("buildx", "stop"), command.buildArgs())
    }

    @Test
    fun `BuilderStopCommand buildArgs with name`() {
        val command = BuilderStopCommand("my-builder")
        assertEquals(listOf("buildx", "stop", "my-builder"), command.buildArgs())
    }

    // BuilderPruneCommand tests

    @Test
    fun `BuilderPruneCommand buildArgs basic`() {
        val command = BuilderPruneCommand()
        assertEquals(listOf("buildx", "prune"), command.buildArgs())
    }

    @Test
    fun `BuilderPruneCommand buildArgs with force`() {
        val command = BuilderPruneCommand().force()
        assertEquals(listOf("buildx", "prune", "--force"), command.buildArgs())
    }

    @Test
    fun `BuilderPruneCommand buildArgs with all`() {
        val command = BuilderPruneCommand().all().force()
        assertEquals(listOf("buildx", "prune", "--all", "--force"), command.buildArgs())
    }

    @Test
    fun `BuilderPruneCommand buildArgs with keep storage`() {
        val command = BuilderPruneCommand()
            .keepStorage("10GB")
            .force()
        assertEquals(
            listOf("buildx", "prune", "--force", "--keep-storage", "10GB"),
            command.buildArgs()
        )
    }

    @Test
    fun `BuilderPruneCommand buildArgs with filters`() {
        val command = BuilderPruneCommand()
            .until("24h")
            .filter("type=regular")
            .force()
        assertEquals(
            listOf("buildx", "prune", "--force", "--filter", "until=24h", "--filter", "type=regular"),
            command.buildArgs()
        )
    }

    @Test
    fun `BuilderPruneCommand buildArgs verbose`() {
        val command = BuilderPruneCommand().force().verbose()
        assertEquals(listOf("buildx", "prune", "--force", "--verbose"), command.buildArgs())
    }
}
