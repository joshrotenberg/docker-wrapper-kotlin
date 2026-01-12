package io.github.joshrotenberg.dockerkotlin.compose

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeCommandsTest {

    // ComposeUpCommand tests

    @Test
    fun `ComposeUpCommand buildArgs basic`() {
        val command = ComposeUpCommand()
        assertEquals(listOf("compose", "up"), command.buildArgs())
    }

    @Test
    fun `ComposeUpCommand buildArgs with detach`() {
        val command = ComposeUpCommand().detach()
        val args = command.buildArgs()
        assertTrue(args.contains("compose"))
        assertTrue(args.contains("up"))
        assertTrue(args.contains("--detach"))
    }

    @Test
    fun `ComposeUpCommand buildArgs with options`() {
        val command = ComposeUpCommand()
            .file("docker-compose.yml")
            .projectName("myapp")
            .detach()
            .build()
            .wait()
        val args = command.buildArgs()
        assertTrue(args.contains("--file"))
        assertTrue(args.contains("docker-compose.yml"))
        assertTrue(args.contains("--project-name"))
        assertTrue(args.contains("myapp"))
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("--build"))
        assertTrue(args.contains("--wait"))
    }

    @Test
    fun `ComposeUpCommand buildArgs with scale`() {
        val command = ComposeUpCommand()
            .scale("web", 3)
            .scale("worker", 2)
        val args = command.buildArgs()
        assertTrue(args.contains("--scale"))
        assertTrue(args.contains("web=3"))
        assertTrue(args.contains("worker=2"))
    }

    // ComposeDownCommand tests

    @Test
    fun `ComposeDownCommand buildArgs basic`() {
        val command = ComposeDownCommand()
        assertEquals(listOf("compose", "down"), command.buildArgs())
    }

    @Test
    fun `ComposeDownCommand buildArgs with volumes`() {
        val command = ComposeDownCommand().volumes()
        assertEquals(listOf("compose", "down", "--volumes"), command.buildArgs())
    }

    @Test
    fun `ComposeDownCommand buildArgs with options`() {
        val command = ComposeDownCommand()
            .volumes()
            .removeOrphans()
            .timeout(30)
        val args = command.buildArgs()
        assertTrue(args.contains("--volumes"))
        assertTrue(args.contains("--remove-orphans"))
        assertTrue(args.contains("--timeout"))
        assertTrue(args.contains("30"))
    }

    // ComposePsCommand tests

    @Test
    fun `ComposePsCommand buildArgs basic`() {
        val command = ComposePsCommand()
        assertEquals(listOf("compose", "ps"), command.buildArgs())
    }

    @Test
    fun `ComposePsCommand buildArgs with format`() {
        val command = ComposePsCommand().format("json")
        val args = command.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    // ComposeBuildCommand tests

    @Test
    fun `ComposeBuildCommand buildArgs basic`() {
        val command = ComposeBuildCommand()
        assertEquals(listOf("compose", "build"), command.buildArgs())
    }

    @Test
    fun `ComposeBuildCommand buildArgs with options`() {
        val command = ComposeBuildCommand()
            .noCache()
            .pull()
            .buildArg("VERSION", "1.0")
            .services("web", "api")
        val args = command.buildArgs()
        assertTrue(args.contains("--no-cache"))
        assertTrue(args.contains("--pull"))
        assertTrue(args.contains("--build-arg"))
        assertTrue(args.contains("VERSION=1.0"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("api"))
    }

    // ComposeConfigCommand tests

    @Test
    fun `ComposeConfigCommand buildArgs basic`() {
        val command = ComposeConfigCommand()
        assertEquals(listOf("compose", "config"), command.buildArgs())
    }

    @Test
    fun `ComposeConfigCommand buildArgs with format`() {
        val command = ComposeConfigCommand().format(ConfigFormat.JSON)
        val args = command.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `ComposeConfigCommand buildArgs services only`() {
        val command = ComposeConfigCommand().services()
        val args = command.buildArgs()
        assertTrue(args.contains("--services"))
    }

    // ComposeCreateCommand tests

    @Test
    fun `ComposeCreateCommand buildArgs basic`() {
        val command = ComposeCreateCommand()
        assertEquals(listOf("compose", "create"), command.buildArgs())
    }

    @Test
    fun `ComposeCreateCommand buildArgs with options`() {
        val command = ComposeCreateCommand()
            .build()
            .forceRecreate()
            .pull(PullPolicy.ALWAYS)
        val args = command.buildArgs()
        assertTrue(args.contains("--build"))
        assertTrue(args.contains("--force-recreate"))
        assertTrue(args.contains("--pull"))
        assertTrue(args.contains("always"))
    }

    // ComposeExecCommand tests

    @Test
    fun `ComposeExecCommand buildArgs basic`() {
        val command = ComposeExecCommand("web").command("ls", "-la")
        val args = command.buildArgs()
        assertTrue(args.contains("exec"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("ls"))
        assertTrue(args.contains("-la"))
    }

    @Test
    fun `ComposeExecCommand buildArgs with options`() {
        val command = ComposeExecCommand("web")
            .detach()
            .user("root")
            .workdir("/app")
            .env("DEBUG", "true")
            .command("npm", "test")
        val args = command.buildArgs()
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("--user"))
        assertTrue(args.contains("root"))
        assertTrue(args.contains("--workdir"))
        assertTrue(args.contains("/app"))
        assertTrue(args.contains("--env"))
        assertTrue(args.contains("DEBUG=true"))
    }

    // ComposeLogsCommand tests

    @Test
    fun `ComposeLogsCommand buildArgs basic`() {
        val command = ComposeLogsCommand()
        assertEquals(listOf("compose", "logs"), command.buildArgs())
    }

    @Test
    fun `ComposeLogsCommand buildArgs with options`() {
        val command = ComposeLogsCommand()
            .follow()
            .tail(100)
            .timestamps()
            .services("web", "api")
        val args = command.buildArgs()
        assertTrue(args.contains("--follow"))
        assertTrue(args.contains("--tail"))
        assertTrue(args.contains("100"))
        assertTrue(args.contains("--timestamps"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("api"))
    }

    // ComposeKillCommand tests

    @Test
    fun `ComposeKillCommand buildArgs basic`() {
        val command = ComposeKillCommand()
        assertEquals(listOf("compose", "kill"), command.buildArgs())
    }

    @Test
    fun `ComposeKillCommand buildArgs with signal`() {
        val command = ComposeKillCommand()
            .signal("SIGTERM")
            .services("web")
        val args = command.buildArgs()
        assertTrue(args.contains("--signal"))
        assertTrue(args.contains("SIGTERM"))
        assertTrue(args.contains("web"))
    }

    // ComposePullCommand tests

    @Test
    fun `ComposePullCommand buildArgs basic`() {
        val command = ComposePullCommand()
        assertEquals(listOf("compose", "pull"), command.buildArgs())
    }

    @Test
    fun `ComposePullCommand buildArgs with options`() {
        val command = ComposePullCommand()
            .quiet()
            .ignorePullFailures()
            .services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("--quiet"))
        assertTrue(args.contains("--ignore-pull-failures"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposePushCommand tests

    @Test
    fun `ComposePushCommand buildArgs basic`() {
        val command = ComposePushCommand()
        assertEquals(listOf("compose", "push"), command.buildArgs())
    }

    @Test
    fun `ComposePushCommand buildArgs with options`() {
        val command = ComposePushCommand()
            .ignorePushFailures()
            .quiet()
            .services("web")
        val args = command.buildArgs()
        assertTrue(args.contains("--ignore-push-failures"))
        assertTrue(args.contains("--quiet"))
        assertTrue(args.contains("web"))
    }

    // ComposeRestartCommand tests

    @Test
    fun `ComposeRestartCommand buildArgs basic`() {
        val command = ComposeRestartCommand()
        assertEquals(listOf("compose", "restart"), command.buildArgs())
    }

    @Test
    fun `ComposeRestartCommand buildArgs with options`() {
        val command = ComposeRestartCommand()
            .timeout(30)
            .noDeps()
            .services("web")
        val args = command.buildArgs()
        assertTrue(args.contains("--timeout"))
        assertTrue(args.contains("30"))
        assertTrue(args.contains("--no-deps"))
        assertTrue(args.contains("web"))
    }

    // ComposeRmCommand tests

    @Test
    fun `ComposeRmCommand buildArgs basic`() {
        val command = ComposeRmCommand()
        assertEquals(listOf("compose", "rm"), command.buildArgs())
    }

    @Test
    fun `ComposeRmCommand buildArgs with options`() {
        val command = ComposeRmCommand()
            .force()
            .stop()
            .volumes()
            .services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("--force"))
        assertTrue(args.contains("--stop"))
        assertTrue(args.contains("--volumes"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposeRunCommand tests

    @Test
    fun `ComposeRunCommand buildArgs basic`() {
        val command = ComposeRunCommand("web").command("echo", "hello")
        val args = command.buildArgs()
        assertTrue(args.contains("run"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("echo"))
        assertTrue(args.contains("hello"))
    }

    @Test
    fun `ComposeRunCommand buildArgs with options`() {
        val command = ComposeRunCommand("web")
            .rm()
            .detach()
            .user("node")
            .env("NODE_ENV", "test")
            .command("npm", "test")
        val args = command.buildArgs()
        assertTrue(args.contains("--rm"))
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("--user"))
        assertTrue(args.contains("node"))
        assertTrue(args.contains("--env"))
        assertTrue(args.contains("NODE_ENV=test"))
    }

    // ComposeStartCommand tests

    @Test
    fun `ComposeStartCommand buildArgs basic`() {
        val command = ComposeStartCommand()
        assertEquals(listOf("compose", "start"), command.buildArgs())
    }

    @Test
    fun `ComposeStartCommand buildArgs with services`() {
        val command = ComposeStartCommand().services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposeStopCommand tests

    @Test
    fun `ComposeStopCommand buildArgs basic`() {
        val command = ComposeStopCommand()
        assertEquals(listOf("compose", "stop"), command.buildArgs())
    }

    @Test
    fun `ComposeStopCommand buildArgs with options`() {
        val command = ComposeStopCommand()
            .timeout(30)
            .services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("--timeout"))
        assertTrue(args.contains("30"))
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposePauseCommand tests

    @Test
    fun `ComposePauseCommand buildArgs basic`() {
        val command = ComposePauseCommand()
        assertEquals(listOf("compose", "pause"), command.buildArgs())
    }

    @Test
    fun `ComposePauseCommand buildArgs with services`() {
        val command = ComposePauseCommand().services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposeUnpauseCommand tests

    @Test
    fun `ComposeUnpauseCommand buildArgs basic`() {
        val command = ComposeUnpauseCommand()
        assertEquals(listOf("compose", "unpause"), command.buildArgs())
    }

    @Test
    fun `ComposeUnpauseCommand buildArgs with services`() {
        val command = ComposeUnpauseCommand().services("web", "db")
        val args = command.buildArgs()
        assertTrue(args.contains("web"))
        assertTrue(args.contains("db"))
    }

    // ComposeCpCommand tests

    @Test
    fun `ComposeCpCommand buildArgs basic`() {
        val command = ComposeCpCommand("web:/app/file.txt", "./file.txt")
        val args = command.buildArgs()
        assertTrue(args.contains("cp"))
        assertTrue(args.contains("web:/app/file.txt"))
        assertTrue(args.contains("./file.txt"))
    }

    @Test
    fun `ComposeCpCommand buildArgs with options`() {
        val command = ComposeCpCommand("./file.txt", "web:/app/file.txt")
            .archive()
            .followLink()
            .index(2)
        val args = command.buildArgs()
        assertTrue(args.contains("--archive"))
        assertTrue(args.contains("--follow-link"))
        assertTrue(args.contains("--index"))
        assertTrue(args.contains("2"))
    }
}
