package io.github.joshrotenberg.dockerkotlin.core.command.network

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for network command argument building.
 */
class NetworkCommandsTest {

    @Test
    fun `NetworkCreateCommand buildArgs basic`() {
        val cmd = NetworkCreateCommand("my-network")
        assertEquals(listOf("network", "create", "my-network"), cmd.buildArgs())
    }

    @Test
    fun `NetworkCreateCommand buildArgs with driver`() {
        val cmd = NetworkCreateCommand("my-network")
            .driver("overlay")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--driver"))
        assertTrue(args.contains("overlay"))
    }

    @Test
    fun `NetworkCreateCommand buildArgs with subnet and gateway`() {
        val cmd = NetworkCreateCommand("my-network")
            .subnet("172.20.0.0/16")
            .gateway("172.20.0.1")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--subnet"))
        assertTrue(args.contains("172.20.0.0/16"))
        assertTrue(args.contains("--gateway"))
        assertTrue(args.contains("172.20.0.1"))
    }

    @Test
    fun `NetworkCreateCommand buildArgs with all options`() {
        val cmd = NetworkCreateCommand("my-network")
            .driver("bridge")
            .driverOpt("com.docker.network.bridge.name", "br0")
            .subnet("172.20.0.0/16")
            .ipRange("172.20.240.0/20")
            .gateway("172.20.0.1")
            .ipv6()
            .attachable()
            .internal()
            .label("env", "test")
            .auxAddress("host1", "172.20.0.5")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--driver"))
        assertTrue(args.contains("--ipv6"))
        assertTrue(args.contains("--attachable"))
        assertTrue(args.contains("--internal"))
        assertTrue(args.contains("--label"))
        assertTrue(args.contains("env=test"))
    }

    @Test
    fun `NetworkLsCommand buildArgs basic`() {
        val cmd = NetworkLsCommand()
        val args = cmd.buildArgs()
        assertEquals("network", args[0])
        assertEquals("ls", args[1])
        // Default format is json
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `NetworkLsCommand buildArgs with filters`() {
        val cmd = NetworkLsCommand()
            .driverFilter("bridge")
            .nameFilter("my-network")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("driver=bridge") })
        assertTrue(args.any { it.contains("name=my-network") })
    }

    @Test
    fun `NetworkLsCommand buildArgs with format`() {
        val cmd = NetworkLsCommand().format("{{.Name}}")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Name}}"))
    }

    @Test
    fun `NetworkLsCommand buildArgs default uses json format`() {
        val cmd = NetworkLsCommand()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `NetworkLsCommand buildArgs quiet`() {
        val cmd = NetworkLsCommand().quiet()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `NetworkRmCommand buildArgs single`() {
        val cmd = NetworkRmCommand("my-network")
        assertEquals(listOf("network", "rm", "my-network"), cmd.buildArgs())
    }

    @Test
    fun `NetworkRmCommand buildArgs multiple`() {
        val cmd = NetworkRmCommand(listOf("net1", "net2"))
        val args = cmd.buildArgs()
        assertEquals("network", args[0])
        assertEquals("rm", args[1])
        assertTrue(args.contains("net1"))
        assertTrue(args.contains("net2"))
    }

    @Test
    fun `NetworkRmCommand buildArgs with force`() {
        val cmd = NetworkRmCommand("my-network").force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `NetworkInspectCommand buildArgs single`() {
        val cmd = NetworkInspectCommand("my-network")
        assertEquals(listOf("network", "inspect", "my-network"), cmd.buildArgs())
    }

    @Test
    fun `NetworkInspectCommand buildArgs multiple`() {
        val cmd = NetworkInspectCommand(listOf("net1", "net2"))
        val args = cmd.buildArgs()
        assertTrue(args.contains("net1"))
        assertTrue(args.contains("net2"))
    }

    @Test
    fun `NetworkInspectCommand buildArgs with format`() {
        val cmd = NetworkInspectCommand("my-network")
            .format("{{.Driver}}")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Driver}}"))
    }

    @Test
    fun `NetworkInspectCommand buildArgs verbose`() {
        val cmd = NetworkInspectCommand("my-network").verbose()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--verbose"))
    }

    @Test
    fun `NetworkConnectCommand buildArgs basic`() {
        val cmd = NetworkConnectCommand("my-network", "my-container")
        assertEquals(listOf("network", "connect", "my-network", "my-container"), cmd.buildArgs())
    }

    @Test
    fun `NetworkConnectCommand buildArgs with ip`() {
        val cmd = NetworkConnectCommand("my-network", "my-container")
            .ipv4("172.20.0.10")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--ip"))
        assertTrue(args.contains("172.20.0.10"))
    }

    @Test
    fun `NetworkConnectCommand buildArgs with alias`() {
        val cmd = NetworkConnectCommand("my-network", "my-container")
            .alias("db")
            .alias("database")

        val args = cmd.buildArgs()
        assertEquals(2, args.count { it == "--alias" })
        assertTrue(args.contains("db"))
        assertTrue(args.contains("database"))
    }

    @Test
    fun `NetworkDisconnectCommand buildArgs basic`() {
        val cmd = NetworkDisconnectCommand("my-network", "my-container")
        assertEquals(listOf("network", "disconnect", "my-network", "my-container"), cmd.buildArgs())
    }

    @Test
    fun `NetworkDisconnectCommand buildArgs with force`() {
        val cmd = NetworkDisconnectCommand("my-network", "my-container").force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `NetworkPruneCommand buildArgs basic`() {
        val cmd = NetworkPruneCommand()
        assertEquals(listOf("network", "prune"), cmd.buildArgs())
    }

    @Test
    fun `NetworkPruneCommand buildArgs with force`() {
        val cmd = NetworkPruneCommand().force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `NetworkPruneCommand buildArgs with filters`() {
        val cmd = NetworkPruneCommand()
            .until("24h")
            .labelFilter("env=test")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("until=24h") })
        assertTrue(args.any { it.contains("label=env=test") })
    }
}
