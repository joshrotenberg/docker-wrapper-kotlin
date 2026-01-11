package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ImagePruneCommand argument building.
 */
class ImagePruneCommandTest {

    @Test
    fun `ImagePruneCommand buildArgs basic`() {
        val cmd = ImagePruneCommand()
        assertEquals(listOf("image", "prune"), cmd.buildArgs())
    }

    @Test
    fun `ImagePruneCommand buildArgs with force`() {
        val cmd = ImagePruneCommand().force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
    }

    @Test
    fun `ImagePruneCommand buildArgs with all`() {
        val cmd = ImagePruneCommand().all()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
    }

    @Test
    fun `ImagePruneCommand buildArgs with until`() {
        val cmd = ImagePruneCommand().until("24h")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("until=24h") })
    }

    @Test
    fun `ImagePruneCommand buildArgs with label filter`() {
        val cmd = ImagePruneCommand().labelFilter("deprecated")
        val args = cmd.buildArgs()
        assertTrue(args.contains("--filter"))
        assertTrue(args.any { it.contains("label=deprecated") })
    }

    @Test
    fun `ImagePruneCommand buildArgs dangling only`() {
        val cmd = ImagePruneCommand().danglingOnly().force()
        val args = cmd.buildArgs()
        assertTrue(args.contains("--force"))
        assertTrue(args.any { it.contains("dangling=true") })
    }

    @Test
    fun `ImagePruneCommand buildArgs all options`() {
        val cmd = ImagePruneCommand()
            .all()
            .force()
            .until("7d")
            .labelFilter("env=test")

        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
        assertTrue(args.contains("--force"))
        assertTrue(args.any { it.contains("until=7d") })
        assertTrue(args.any { it.contains("label=env=test") })
    }
}
