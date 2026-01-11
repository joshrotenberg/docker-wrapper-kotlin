package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunCommandTest {

    @Test
    fun `buildArgs with minimal configuration`() {
        val cmd = RunCommand("nginx:alpine")
        val args = cmd.buildArgs()

        assertEquals(listOf("run", "nginx:alpine"), args)
    }

    @Test
    fun `buildArgs with name and detach`() {
        val cmd = RunCommand("nginx:alpine")
            .name("web-server")
            .detach()

        val args = cmd.buildArgs()

        assertTrue(args.contains("--name"))
        assertTrue(args.contains("web-server"))
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("nginx:alpine"))
    }

    @Test
    fun `buildArgs with port mapping`() {
        val cmd = RunCommand("nginx:alpine")
            .port(8080, 80)

        val args = cmd.buildArgs()

        assertTrue(args.contains("--publish"))
        assertTrue(args.contains("8080:80"))
    }

    @Test
    fun `buildArgs with environment variables`() {
        val cmd = RunCommand("nginx:alpine")
            .env("KEY1", "value1")
            .env("KEY2", "value2")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--env"))
        assertTrue(args.contains("KEY1=value1"))
        assertTrue(args.contains("KEY2=value2"))
    }

    @Test
    fun `buildArgs with volume mount`() {
        val cmd = RunCommand("nginx:alpine")
            .volume("/host/path", "/container/path")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--volume"))
        assertTrue(args.contains("/host/path:/container/path"))
    }

    @Test
    fun `buildArgs with resource limits`() {
        val cmd = RunCommand("nginx:alpine")
            .memory("512m")
            .cpus("0.5")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--memory"))
        assertTrue(args.contains("512m"))
        assertTrue(args.contains("--cpus"))
        assertTrue(args.contains("0.5"))
    }

    @Test
    fun `buildArgs with command`() {
        val cmd = RunCommand("alpine")
            .command("echo", "hello", "world")

        val args = cmd.buildArgs()

        assertEquals("alpine", args[args.indexOf("alpine")])
        assertTrue(args.containsAll(listOf("echo", "hello", "world")))
    }

    @Test
    fun `buildArgs with labels`() {
        val cmd = RunCommand("nginx:alpine")
            .label("app", "web")
            .label("env", "test")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--label"))
        assertTrue(args.contains("app=web"))
        assertTrue(args.contains("env=test"))
    }

    @Test
    fun `buildArgs with restart policy`() {
        val cmd = RunCommand("nginx:alpine")
            .restart(RestartPolicy.OnFailure(maxRetries = 5))

        val args = cmd.buildArgs()

        assertTrue(args.contains("--restart"))
        assertTrue(args.contains("on-failure:5"))
    }

    @Test
    fun `preview returns command line`() {
        val cmd = RunCommand("nginx:alpine")
            .name("web")
            .detach()

        val preview = cmd.preview()

        assertTrue(preview.commandLine.startsWith("docker run"))
        assertTrue(preview.commandLine.contains("--name"))
        assertTrue(preview.commandLine.contains("web"))
        assertTrue(preview.commandLine.contains("--detach"))
        assertTrue(preview.commandLine.contains("nginx:alpine"))
    }

    @Test
    fun `buildArgs with full configuration`() {
        val cmd = RunCommand("nginx:alpine")
            .name("web-server")
            .port(8080, 80)
            .port(8443, 443, Protocol.TCP)
            .env("WORKER_PROCESSES", "4")
            .volume("/data", "/app/data")
            .label("app", "nginx")
            .detach()
            .rm()
            .network("my-network")
            .hostname("web")
            .memory("1g")
            .cpus("2")
            .restart(RestartPolicy.Always)
            .init()

        val args = cmd.buildArgs()

        assertTrue(args.contains("run"))
        assertTrue(args.contains("--name"))
        assertTrue(args.contains("--publish"))
        assertTrue(args.contains("--env"))
        assertTrue(args.contains("--volume"))
        assertTrue(args.contains("--label"))
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("--rm"))
        assertTrue(args.contains("--network"))
        assertTrue(args.contains("--hostname"))
        assertTrue(args.contains("--memory"))
        assertTrue(args.contains("--cpus"))
        assertTrue(args.contains("--restart"))
        assertTrue(args.contains("--init"))
        assertTrue(args.last() == "nginx:alpine" || args.contains("nginx:alpine"))
    }
}
