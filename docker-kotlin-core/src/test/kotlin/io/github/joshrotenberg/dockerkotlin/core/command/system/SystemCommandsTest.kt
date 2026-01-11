package io.github.joshrotenberg.dockerkotlin.core.command.system

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for system command argument building.
 */
class SystemCommandsTest {

    @Test
    fun `SystemDfCommand buildArgs basic`() {
        val cmd = SystemDfCommand()
        assertEquals(listOf("system", "df"), cmd.buildArgs())
    }

    @Test
    fun `SystemDfCommand buildArgs verbose`() {
        val cmd = SystemDfCommand().verbose()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--verbose"))
    }

    @Test
    fun `SystemDfCommand buildArgs with format`() {
        val cmd = SystemDfCommand().format("{{.Type}}")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Type}}"))
    }

    @Test
    fun `SystemDfCommand buildArgs format json`() {
        val cmd = SystemDfCommand().formatJson()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    @Test
    fun `SystemPruneCommand buildArgs basic`() {
        val cmd = SystemPruneCommand()
        assertEquals(listOf("system", "prune"), cmd.buildArgs())
    }

    @Test
    fun `SystemPruneCommand buildArgs with force`() {
        val cmd = SystemPruneCommand().force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `SystemPruneCommand buildArgs with all`() {
        val cmd = SystemPruneCommand().all()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
    }

    @Test
    fun `SystemPruneCommand buildArgs with volumes`() {
        val cmd = SystemPruneCommand().volumes()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--volumes"))
    }

    @Test
    fun `SystemPruneCommand buildArgs with all options`() {
        val cmd = SystemPruneCommand()
            .all()
            .volumes()
            .force()
            .until("24h")
            .labelFilter("env=test")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
        assertTrue(args.contains("--volumes"))
        assertTrue(args.contains("--force"))
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("until=24h") })
        assertTrue(args.any { it.contains("label=env=test") })
    }
}
