package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for container command argument building.
 */
class ContainerCommandsTest {

    @Test
    fun `StartCommand buildArgs basic`() {
        val cmd = StartCommand("my-container")
        assertEquals(listOf("start", "my-container"), cmd.buildArgs())
    }

    @Test
    fun `StartCommand buildArgs with options`() {
        val cmd = StartCommand("my-container")
            .attach()
            .interactive()
            .detachKeys("ctrl-p,ctrl-q")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--attach"))
        assertTrue(args.contains("--interactive"))
        assertTrue(args.contains("--detach-keys"))
        assertTrue(args.contains("ctrl-p,ctrl-q"))
    }

    @Test
    fun `StopCommand buildArgs with time`() {
        val cmd = StopCommand("my-container").time(5)
        val args = cmd.buildArgs()
        assertTrue(args.contains("--time"))
        assertTrue(args.contains("5"))
    }

    @Test
    fun `RestartCommand buildArgs with signal`() {
        val cmd = RestartCommand(listOf("c1", "c2")).signal("SIGTERM")
        val args = cmd.buildArgs()
        assertEquals("restart", args[0])
        assertTrue(args.contains("--signal"))
        assertTrue(args.contains("SIGTERM"))
        assertTrue(args.contains("c1"))
        assertTrue(args.contains("c2"))
    }

    @Test
    fun `KillCommand buildArgs with signal`() {
        val cmd = KillCommand("my-container").signal("SIGKILL")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--signal"))
        assertTrue(args.contains("SIGKILL"))
    }

    @Test
    fun `PauseCommand buildArgs`() {
        val cmd = PauseCommand(listOf("c1", "c2"))
        assertEquals(listOf("pause", "c1", "c2"), cmd.buildArgs())
    }

    @Test
    fun `UnpauseCommand buildArgs`() {
        val cmd = UnpauseCommand("my-container")
        assertEquals(listOf("unpause", "my-container"), cmd.buildArgs())
    }

    @Test
    fun `WaitCommand buildArgs`() {
        val cmd = WaitCommand(listOf("c1", "c2"))
        assertEquals(listOf("wait", "c1", "c2"), cmd.buildArgs())
    }

    @Test
    fun `RenameCommand buildArgs`() {
        val cmd = RenameCommand("old-name", "new-name")
        assertEquals(listOf("rename", "old-name", "new-name"), cmd.buildArgs())
    }

    @Test
    fun `ExecCommand buildArgs basic`() {
        val cmd = ExecCommand("my-container", "ls", "-la")
        val args = cmd.buildArgs()
        assertEquals("exec", args[0])
        assertTrue(args.contains("my-container"))
        assertTrue(args.contains("ls"))
        assertTrue(args.contains("-la"))
    }

    @Test
    fun `ExecCommand buildArgs with options`() {
        val cmd = ExecCommand("my-container", listOf("bash"))
            .it()
            .user("root")
            .workdir("/app")
            .env("DEBUG", "1")
            .privileged()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--interactive"))
        assertTrue(args.contains("--tty"))
        assertTrue(args.contains("--user"))
        assertTrue(args.contains("root"))
        assertTrue(args.contains("--workdir"))
        assertTrue(args.contains("/app"))
        assertTrue(args.contains("--env"))
        assertTrue(args.contains("DEBUG=1"))
        assertTrue(args.contains("--privileged"))
    }

    @Test
    fun `LogsCommand buildArgs with options`() {
        val cmd = LogsCommand("my-container")
            .tail(100)
            .timestamps()
            .since("1h")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--tail"))
        assertTrue(args.contains("100"))
        assertTrue(args.contains("--timestamps"))
        assertTrue(args.contains("--since"))
        assertTrue(args.contains("1h"))
    }

    @Test
    fun `PsCommand buildArgs with filters`() {
        val cmd = PsCommand()
            .all()
            .filterName("my-app")
            .filterStatus("running")
            .quiet()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
        assertTrue(args.contains("--filter"))
        assertTrue(args.contains("name=my-app"))
        assertTrue(args.contains("status=running"))
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `InspectCommand buildArgs with format`() {
        val cmd = InspectCommand("my-container")
            .format("{{.State.Running}}")
            .size()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.State.Running}}"))
        assertTrue(args.contains("--size"))
    }

    @Test
    fun `TopCommand buildArgs`() {
        val cmd = TopCommand("my-container").psOptions("-aux")
        val args = cmd.buildArgs()
        assertEquals(listOf("top", "my-container", "-aux"), args)
    }

    @Test
    fun `StatsCommand buildArgs`() {
        val cmd = StatsCommand(listOf("c1", "c2"))
            .noStream()
            .noTrunc()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--no-stream"))
        assertTrue(args.contains("--no-trunc"))
        assertTrue(args.contains("c1"))
        assertTrue(args.contains("c2"))
    }

    @Test
    fun `PortCommand buildArgs`() {
        val cmd = PortCommand("my-container")
            .privatePort(80)
            .protocol("tcp")

        val args = cmd.buildArgs()
        assertEquals(listOf("port", "my-container", "80/tcp"), args)
    }

    @Test
    fun `DiffCommand buildArgs`() {
        val cmd = DiffCommand("my-container")
        assertEquals(listOf("diff", "my-container"), cmd.buildArgs())
    }

    @Test
    fun `CpCommand buildArgs from container`() {
        val cmd = CpCommand.fromContainer("my-container", "/app/data", "/tmp/data")
            .archive()
            .quiet()

        val args = cmd.buildArgs()
        assertEquals("cp", args[0])
        assertTrue(args.contains("--archive"))
        assertTrue(args.contains("--quiet"))
        assertTrue(args.contains("my-container:/app/data"))
        assertTrue(args.contains("/tmp/data"))
    }

    @Test
    fun `CpCommand buildArgs to container`() {
        val cmd = CpCommand.toContainer("/tmp/data", "my-container", "/app/data")
        val args = cmd.buildArgs()
        assertTrue(args.contains("/tmp/data"))
        assertTrue(args.contains("my-container:/app/data"))
    }

    @Test
    fun `AttachCommand buildArgs`() {
        val cmd = AttachCommand("my-container")
            .noStdin()
            .detachKeys("ctrl-c")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--no-stdin"))
        assertTrue(args.contains("--detach-keys"))
        assertTrue(args.contains("ctrl-c"))
    }

    @Test
    fun `CommitCommand buildArgs`() {
        val cmd = CommitCommand("my-container", "my-image:v1")
            .author("John Doe")
            .message("Added files")
            .change("ENV DEBUG=1")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--author"))
        assertTrue(args.contains("John Doe"))
        assertTrue(args.contains("--message"))
        assertTrue(args.contains("Added files"))
        assertTrue(args.contains("--change"))
        assertTrue(args.contains("ENV DEBUG=1"))
        assertTrue(args.contains("my-container"))
        assertTrue(args.contains("my-image:v1"))
    }

    @Test
    fun `ExportCommand buildArgs`() {
        val cmd = ExportCommand("my-container").output("/tmp/export.tar")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--output"))
        assertTrue(args.contains("/tmp/export.tar"))
    }

    @Test
    fun `CreateCommand buildArgs`() {
        val cmd = CreateCommand("nginx:alpine")
            .name("web")
            .publish(8080, 80)
            .env("KEY", "value")
            .volume("/host", "/container")
            .network("my-net")
            .command("nginx", "-g", "daemon off;")

        val args = cmd.buildArgs()
        assertEquals("create", args[0])
        assertTrue(args.contains("--name"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("--publish"))
        assertTrue(args.contains("8080:80"))
        assertTrue(args.contains("--env"))
        assertTrue(args.contains("KEY=value"))
        assertTrue(args.contains("--volume"))
        assertTrue(args.contains("/host:/container"))
        assertTrue(args.contains("--network"))
        assertTrue(args.contains("my-net"))
        assertTrue(args.contains("nginx:alpine"))
        assertTrue(args.contains("nginx"))
    }

    @Test
    fun `UpdateCommand buildArgs`() {
        val cmd = UpdateCommand("my-container")
            .memory("512m")
            .cpus("1.5")
            .restart("always")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--memory"))
        assertTrue(args.contains("512m"))
        assertTrue(args.contains("--cpus"))
        assertTrue(args.contains("1.5"))
        assertTrue(args.contains("--restart"))
        assertTrue(args.contains("always"))
    }
}
