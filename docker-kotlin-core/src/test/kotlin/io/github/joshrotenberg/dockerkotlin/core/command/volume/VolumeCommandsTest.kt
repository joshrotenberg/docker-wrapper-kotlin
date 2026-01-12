package io.github.joshrotenberg.dockerkotlin.core.command.volume

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for volume command argument building.
 */
class VolumeCommandsTest {

    @Test
    fun `VolumeCreateCommand buildArgs basic`() {
        val cmd = VolumeCreateCommand()
        assertEquals(listOf("volume", "create"), cmd.buildArgs())
    }

    @Test
    fun `VolumeCreateCommand buildArgs with name`() {
        val cmd = VolumeCreateCommand().name("my-volume")
        assertEquals(listOf("volume", "create", "my-volume"), cmd.buildArgs())
    }

    @Test
    fun `VolumeCreateCommand buildArgs with driver`() {
        val cmd = VolumeCreateCommand()
            .name("my-volume")
            .driver("local")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--driver"))
        assertTrue(args.contains("local"))
    }

    @Test
    fun `VolumeCreateCommand buildArgs with driver opts`() {
        val cmd = VolumeCreateCommand()
            .name("my-volume")
            .driverOpt("type", "nfs")
            .driverOpt("o", "addr=10.0.0.1")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--opt"))
        assertTrue(args.any { it.contains("type=nfs") })
        assertTrue(args.any { it.contains("o=addr=10.0.0.1") })
    }

    @Test
    fun `VolumeCreateCommand buildArgs with labels`() {
        val cmd = VolumeCreateCommand()
            .name("my-volume")
            .label("env", "production")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--label"))
        assertTrue(args.any { it.contains("env=production") })
    }

    @Test
    fun `VolumeLsCommand buildArgs basic`() {
        val cmd = VolumeLsCommand()
        val args = cmd.buildArgs()
        assertEquals("volume", args[0])
        assertEquals("ls", args[1])
        // Default format is json
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `VolumeLsCommand buildArgs with filters`() {
        val cmd = VolumeLsCommand()
            .driverFilter("local")
            .nameFilter("my-vol")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("driver=local") })
        assertTrue(args.any { it.contains("name=my-vol") })
    }

    @Test
    fun `VolumeLsCommand buildArgs quiet`() {
        val cmd = VolumeLsCommand().quiet()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `VolumeLsCommand buildArgs format custom`() {
        val cmd = VolumeLsCommand().format("{{.Name}}")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Name}}"))
    }

    @Test
    fun `VolumeLsCommand buildArgs default uses json format`() {
        val cmd = VolumeLsCommand()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `VolumeLsCommand buildArgs dangling filter`() {
        val cmd = VolumeLsCommand().danglingFilter()
        val args = cmd.buildArgs()
        assertTrue(args.any { it.contains("dangling=true") })
    }

    @Test
    fun `VolumeRmCommand buildArgs single`() {
        val cmd = VolumeRmCommand("my-volume")
        assertEquals(listOf("volume", "rm", "my-volume"), cmd.buildArgs())
    }

    @Test
    fun `VolumeRmCommand buildArgs multiple`() {
        val cmd = VolumeRmCommand(listOf("vol1", "vol2"))
        val args = cmd.buildArgs()
        assertEquals("volume", args[0])
        assertEquals("rm", args[1])
        assertTrue(args.contains("vol1"))
        assertTrue(args.contains("vol2"))
    }

    @Test
    fun `VolumeRmCommand buildArgs with force`() {
        val cmd = VolumeRmCommand("my-volume").force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `VolumeInspectCommand buildArgs single`() {
        val cmd = VolumeInspectCommand("my-volume")
        assertEquals(listOf("volume", "inspect", "my-volume"), cmd.buildArgs())
    }

    @Test
    fun `VolumeInspectCommand buildArgs multiple`() {
        val cmd = VolumeInspectCommand(listOf("vol1", "vol2"))
        val args = cmd.buildArgs()
        assertTrue(args.contains("vol1"))
        assertTrue(args.contains("vol2"))
    }

    @Test
    fun `VolumeInspectCommand buildArgs with format`() {
        val cmd = VolumeInspectCommand("my-volume")
            .format("{{.Mountpoint}}")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Mountpoint}}"))
    }

    @Test
    fun `VolumePruneCommand buildArgs basic`() {
        val cmd = VolumePruneCommand()
        assertEquals(listOf("volume", "prune"), cmd.buildArgs())
    }

    @Test
    fun `VolumePruneCommand buildArgs with force`() {
        val cmd = VolumePruneCommand().force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `VolumePruneCommand buildArgs with all`() {
        val cmd = VolumePruneCommand().all()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
    }

    @Test
    fun `VolumePruneCommand buildArgs with filters`() {
        val cmd = VolumePruneCommand()
            .labelFilter("env=test")
            .force()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("label=env=test") })
    }
}
