package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwarmCommandsTest {

    // SwarmInitCommand tests

    @Test
    fun `SwarmInitCommand buildArgs basic`() {
        val command = SwarmInitCommand()
        assertEquals(listOf("swarm", "init"), command.buildArgs())
    }

    @Test
    fun `SwarmInitCommand buildArgs with advertise addr`() {
        val command = SwarmInitCommand()
            .advertiseAddr("192.168.1.100")
        val args = command.buildArgs()
        assertTrue(args.contains("--advertise-addr"))
        assertTrue(args.contains("192.168.1.100"))
    }

    @Test
    fun `SwarmInitCommand buildArgs with autolock`() {
        val command = SwarmInitCommand()
            .autolock()
        val args = command.buildArgs()
        assertTrue(args.contains("--autolock"))
    }

    @Test
    fun `SwarmInitCommand buildArgs with availability`() {
        val command = SwarmInitCommand()
            .availability(NodeAvailability.DRAIN)
        val args = command.buildArgs()
        assertTrue(args.contains("--availability"))
        assertTrue(args.contains("drain"))
    }

    @Test
    fun `SwarmInitCommand buildArgs with all options`() {
        val command = SwarmInitCommand()
            .advertiseAddr("192.168.1.100:2377")
            .autolock()
            .certExpiry("8760h")
            .dataPathAddr("eth0")
            .dataPathPort(4789)
            .listenAddr("0.0.0.0:2377")
            .forceNewCluster()
            .taskHistoryLimit(10)
        val args = command.buildArgs()
        assertTrue(args.contains("--advertise-addr"))
        assertTrue(args.contains("--autolock"))
        assertTrue(args.contains("--cert-expiry"))
        assertTrue(args.contains("--data-path-addr"))
        assertTrue(args.contains("--data-path-port"))
        assertTrue(args.contains("--listen-addr"))
        assertTrue(args.contains("--force-new-cluster"))
        assertTrue(args.contains("--task-history-limit"))
    }

    // SwarmJoinCommand tests

    @Test
    fun `SwarmJoinCommand buildArgs basic`() {
        val command = SwarmJoinCommand("192.168.1.100:2377")
        val args = command.buildArgs()
        assertEquals("swarm", args[0])
        assertEquals("join", args[1])
        assertTrue(args.contains("192.168.1.100:2377"))
    }

    @Test
    fun `SwarmJoinCommand buildArgs with token`() {
        val command = SwarmJoinCommand("192.168.1.100:2377")
            .token("SWMTKN-1-xxx")
        val args = command.buildArgs()
        assertTrue(args.contains("--token"))
        assertTrue(args.contains("SWMTKN-1-xxx"))
    }

    @Test
    fun `SwarmJoinCommand buildArgs with all options`() {
        val command = SwarmJoinCommand("192.168.1.100:2377")
            .token("SWMTKN-1-xxx")
            .advertiseAddr("192.168.1.101")
            .availability(NodeAvailability.ACTIVE)
            .dataPathAddr("eth0")
            .listenAddr("0.0.0.0:2377")
        val args = command.buildArgs()
        assertTrue(args.contains("--token"))
        assertTrue(args.contains("--advertise-addr"))
        assertTrue(args.contains("--availability"))
        assertTrue(args.contains("--data-path-addr"))
        assertTrue(args.contains("--listen-addr"))
    }

    // SwarmLeaveCommand tests

    @Test
    fun `SwarmLeaveCommand buildArgs basic`() {
        val command = SwarmLeaveCommand()
        assertEquals(listOf("swarm", "leave"), command.buildArgs())
    }

    @Test
    fun `SwarmLeaveCommand buildArgs with force`() {
        val command = SwarmLeaveCommand()
            .force()
        val args = command.buildArgs()
        assertTrue(args.contains("--force"))
    }

    // SwarmJoinTokenCommand tests

    @Test
    fun `SwarmJoinTokenCommand buildArgs worker`() {
        val command = SwarmJoinTokenCommand(TokenType.WORKER)
        val args = command.buildArgs()
        assertEquals("swarm", args[0])
        assertEquals("join-token", args[1])
        assertTrue(args.contains("worker"))
    }

    @Test
    fun `SwarmJoinTokenCommand buildArgs manager`() {
        val command = SwarmJoinTokenCommand(TokenType.MANAGER)
        val args = command.buildArgs()
        assertTrue(args.contains("manager"))
    }

    @Test
    fun `SwarmJoinTokenCommand buildArgs with quiet`() {
        val command = SwarmJoinTokenCommand(TokenType.WORKER)
            .quiet()
        val args = command.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `SwarmJoinTokenCommand buildArgs with rotate`() {
        val command = SwarmJoinTokenCommand(TokenType.MANAGER)
            .rotate()
        val args = command.buildArgs()
        assertTrue(args.contains("--rotate"))
    }

    @Test
    fun `SwarmJoinTokenCommand factory methods`() {
        val workerCmd = SwarmJoinTokenCommand.worker()
        assertTrue(workerCmd.buildArgs().contains("worker"))

        val managerCmd = SwarmJoinTokenCommand.manager()
        assertTrue(managerCmd.buildArgs().contains("manager"))
    }

    // SwarmUpdateCommand tests

    @Test
    fun `SwarmUpdateCommand buildArgs basic`() {
        val command = SwarmUpdateCommand()
        assertEquals(listOf("swarm", "update"), command.buildArgs())
    }

    @Test
    fun `SwarmUpdateCommand buildArgs with autolock`() {
        val command = SwarmUpdateCommand()
            .autolock(true)
        val args = command.buildArgs()
        assertTrue(args.contains("--autolock=true"))
    }

    @Test
    fun `SwarmUpdateCommand buildArgs with task history limit`() {
        val command = SwarmUpdateCommand()
            .taskHistoryLimit(10)
        val args = command.buildArgs()
        assertTrue(args.contains("--task-history-limit"))
        assertTrue(args.contains("10"))
    }

    @Test
    fun `SwarmUpdateCommand buildArgs with all options`() {
        val command = SwarmUpdateCommand()
            .autolock(false)
            .certExpiry("8760h")
            .dispatcherHeartbeat("10s")
            .maxSnapshots(5)
            .snapshotInterval(10000)
            .taskHistoryLimit(5)
        val args = command.buildArgs()
        assertTrue(args.contains("--autolock=false"))
        assertTrue(args.contains("--cert-expiry"))
        assertTrue(args.contains("--dispatcher-heartbeat"))
        assertTrue(args.contains("--max-snapshots"))
        assertTrue(args.contains("--snapshot-interval"))
        assertTrue(args.contains("--task-history-limit"))
    }

    // SwarmUnlockCommand tests

    @Test
    fun `SwarmUnlockCommand buildArgs`() {
        val command = SwarmUnlockCommand()
        assertEquals(listOf("swarm", "unlock"), command.buildArgs())
    }

    // SwarmUnlockKeyCommand tests

    @Test
    fun `SwarmUnlockKeyCommand buildArgs basic`() {
        val command = SwarmUnlockKeyCommand()
        assertEquals(listOf("swarm", "unlock-key"), command.buildArgs())
    }

    @Test
    fun `SwarmUnlockKeyCommand buildArgs with quiet`() {
        val command = SwarmUnlockKeyCommand()
            .quiet()
        val args = command.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `SwarmUnlockKeyCommand buildArgs with rotate`() {
        val command = SwarmUnlockKeyCommand()
            .rotate()
        val args = command.buildArgs()
        assertTrue(args.contains("--rotate"))
    }

    // SwarmCaCommand tests

    @Test
    fun `SwarmCaCommand buildArgs basic`() {
        val command = SwarmCaCommand()
        assertEquals(listOf("swarm", "ca"), command.buildArgs())
    }

    @Test
    fun `SwarmCaCommand buildArgs with rotate`() {
        val command = SwarmCaCommand()
            .rotate()
        val args = command.buildArgs()
        assertTrue(args.contains("--rotate"))
    }

    @Test
    fun `SwarmCaCommand buildArgs with quiet`() {
        val command = SwarmCaCommand()
            .quiet()
        val args = command.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `SwarmCaCommand buildArgs with all options`() {
        val command = SwarmCaCommand()
            .caCert("/path/to/ca.pem")
            .caKey("/path/to/ca-key.pem")
            .certExpiry("8760h")
            .detach()
            .rotate()
            .quiet()
        val args = command.buildArgs()
        assertTrue(args.contains("--ca-cert"))
        assertTrue(args.contains("/path/to/ca.pem"))
        assertTrue(args.contains("--ca-key"))
        assertTrue(args.contains("/path/to/ca-key.pem"))
        assertTrue(args.contains("--cert-expiry"))
        assertTrue(args.contains("--detach"))
        assertTrue(args.contains("--rotate"))
        assertTrue(args.contains("--quiet"))
    }
}
