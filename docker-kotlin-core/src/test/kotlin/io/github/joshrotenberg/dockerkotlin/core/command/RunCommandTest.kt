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

    // Tests for new high priority options

    @Test
    fun `buildArgs with capabilities`() {
        val cmd = RunCommand("alpine")
            .capAdd("NET_ADMIN")
            .capAdd("SYS_TIME")
            .capDrop("MKNOD")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--cap-add"))
        assertTrue(args.contains("NET_ADMIN"))
        assertTrue(args.contains("SYS_TIME"))
        assertTrue(args.contains("--cap-drop"))
        assertTrue(args.contains("MKNOD"))
    }

    @Test
    fun `buildArgs with mount`() {
        val cmd = RunCommand("alpine")
            .mount("type=bind,source=/src,target=/app")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--mount"))
        assertTrue(args.contains("type=bind,source=/src,target=/app"))
    }

    @Test
    fun `buildArgs with mount helpers`() {
        val cmd = RunCommand("alpine")
            .mountBind("/src", "/app", readOnly = true)
            .mountVolume("myvolume", "/data")
            .mountTmpfs("/tmp", "100m")

        val args = cmd.buildArgs()

        assertTrue(args.contains("type=bind,source=/src,target=/app,readonly"))
        assertTrue(args.contains("type=volume,source=myvolume,target=/data"))
        assertTrue(args.contains("type=tmpfs,target=/tmp,tmpfs-size=100m"))
    }

    @Test
    fun `buildArgs with health check`() {
        val cmd = RunCommand("nginx:alpine")
            .healthCmd("curl -f http://localhost/ || exit 1")
            .healthInterval("30s")
            .healthTimeout("10s")
            .healthRetries(3)
            .healthStartPeriod("5s")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--health-cmd"))
        assertTrue(args.contains("curl -f http://localhost/ || exit 1"))
        assertTrue(args.contains("--health-interval"))
        assertTrue(args.contains("30s"))
        assertTrue(args.contains("--health-timeout"))
        assertTrue(args.contains("10s"))
        assertTrue(args.contains("--health-retries"))
        assertTrue(args.contains("3"))
        assertTrue(args.contains("--health-start-period"))
        assertTrue(args.contains("5s"))
    }

    @Test
    fun `buildArgs with no healthcheck`() {
        val cmd = RunCommand("alpine")
            .noHealthcheck()

        val args = cmd.buildArgs()

        assertTrue(args.contains("--no-healthcheck"))
    }

    // Tests for new medium priority options

    @Test
    fun `buildArgs with add host`() {
        val cmd = RunCommand("alpine")
            .addHost("myhost", "192.168.1.100")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--add-host"))
        assertTrue(args.contains("myhost:192.168.1.100"))
    }

    @Test
    fun `buildArgs with device`() {
        val cmd = RunCommand("alpine")
            .device("/dev/sda")
            .device("/dev/video0", "/dev/video0")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--device"))
        assertTrue(args.contains("/dev/sda"))
        assertTrue(args.contains("/dev/video0:/dev/video0"))
    }

    @Test
    fun `buildArgs with dns`() {
        val cmd = RunCommand("alpine")
            .dns("8.8.8.8")
            .dns("8.8.4.4")
            .dnsSearch("example.com")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--dns"))
        assertTrue(args.contains("8.8.8.8"))
        assertTrue(args.contains("8.8.4.4"))
        assertTrue(args.contains("--dns-search"))
        assertTrue(args.contains("example.com"))
    }

    @Test
    fun `buildArgs with gpus`() {
        val cmd = RunCommand("nvidia/cuda")
            .gpus("all")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--gpus"))
        assertTrue(args.contains("all"))
    }

    @Test
    fun `buildArgs with logging`() {
        val cmd = RunCommand("nginx:alpine")
            .logDriver("json-file")
            .logOpt("max-size", "10m")
            .logOpt("max-file", "3")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--log-driver"))
        assertTrue(args.contains("json-file"))
        assertTrue(args.contains("--log-opt"))
        assertTrue(args.contains("max-size=10m"))
        assertTrue(args.contains("max-file=3"))
    }

    @Test
    fun `buildArgs with read only`() {
        val cmd = RunCommand("alpine")
            .readOnly()

        val args = cmd.buildArgs()

        assertTrue(args.contains("--read-only"))
    }

    @Test
    fun `buildArgs with security opt`() {
        val cmd = RunCommand("alpine")
            .securityOpt("no-new-privileges")
            .securityOpt("seccomp=unconfined")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--security-opt"))
        assertTrue(args.contains("no-new-privileges"))
        assertTrue(args.contains("seccomp=unconfined"))
    }

    @Test
    fun `buildArgs with tmpfs`() {
        val cmd = RunCommand("alpine")
            .tmpfs("/run")
            .tmpfs("/tmp", "size=100m")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--tmpfs"))
        assertTrue(args.contains("/run"))
        assertTrue(args.contains("/tmp:size=100m"))
    }

    @Test
    fun `buildArgs with ulimit`() {
        val cmd = RunCommand("alpine")
            .ulimit("nofile", 65535, 65535)
            .ulimit("nproc", 1024)

        val args = cmd.buildArgs()

        assertTrue(args.contains("--ulimit"))
        assertTrue(args.contains("nofile=65535:65535"))
        assertTrue(args.contains("nproc=1024:1024"))
    }

    // Tests for new low priority options

    @Test
    fun `buildArgs with shm size`() {
        val cmd = RunCommand("alpine")
            .shmSize("256m")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--shm-size"))
        assertTrue(args.contains("256m"))
    }

    @Test
    fun `buildArgs with namespace options`() {
        val cmd = RunCommand("alpine")
            .ipc("host")
            .pid("host")
            .uts("host")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--ipc"))
        assertTrue(args.contains("--pid"))
        assertTrue(args.contains("--uts"))
    }

    @Test
    fun `buildArgs with stop options`() {
        val cmd = RunCommand("alpine")
            .stopSignal("SIGTERM")
            .stopTimeout(30)

        val args = cmd.buildArgs()

        assertTrue(args.contains("--stop-signal"))
        assertTrue(args.contains("SIGTERM"))
        assertTrue(args.contains("--stop-timeout"))
        assertTrue(args.contains("30"))
    }

    @Test
    fun `buildArgs with sysctl`() {
        val cmd = RunCommand("alpine")
            .sysctl("net.core.somaxconn", "1024")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--sysctl"))
        assertTrue(args.contains("net.core.somaxconn=1024"))
    }

    @Test
    fun `buildArgs with cgroup options`() {
        val cmd = RunCommand("alpine")
            .blkioWeight(500)
            .cgroupParent("/docker/parent")
            .cgroupns("host")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--blkio-weight"))
        assertTrue(args.contains("500"))
        assertTrue(args.contains("--cgroup-parent"))
        assertTrue(args.contains("/docker/parent"))
        assertTrue(args.contains("--cgroupns"))
        assertTrue(args.contains("host"))
    }

    @Test
    fun `buildArgs with mac address`() {
        val cmd = RunCommand("alpine")
            .macAddress("02:42:ac:11:00:02")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--mac-address"))
        assertTrue(args.contains("02:42:ac:11:00:02"))
    }

    @Test
    fun `buildArgs with oom options`() {
        val cmd = RunCommand("alpine")
            .oomKillDisable()
            .oomScoreAdj(-500)

        val args = cmd.buildArgs()

        assertTrue(args.contains("--oom-kill-disable"))
        assertTrue(args.contains("--oom-score-adj"))
        assertTrue(args.contains("-500"))
    }

    @Test
    fun `buildArgs with userns`() {
        val cmd = RunCommand("alpine")
            .userns("host")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--userns"))
        assertTrue(args.contains("host"))
    }

    @Test
    fun `buildArgs with isolation`() {
        val cmd = RunCommand("alpine")
            .isolation("process")

        val args = cmd.buildArgs()

        assertTrue(args.contains("--isolation"))
        assertTrue(args.contains("process"))
    }
}
