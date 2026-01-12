package io.github.joshrotenberg.dockerkotlin.core.command.manifest

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManifestCommandsTest {

    // ManifestCreateCommand tests

    @Test
    fun `ManifestCreateCommand buildArgs basic`() {
        val command = ManifestCreateCommand("myrepo/myimage:latest")
            .manifest("myrepo/myimage:amd64")
        val args = command.buildArgs()
        assertEquals("manifest", args[0])
        assertEquals("create", args[1])
        assertTrue(args.contains("myrepo/myimage:latest"))
        assertTrue(args.contains("myrepo/myimage:amd64"))
    }

    @Test
    fun `ManifestCreateCommand buildArgs with multiple manifests`() {
        val command = ManifestCreateCommand("myrepo/myimage:latest")
            .manifests("myrepo/myimage:amd64", "myrepo/myimage:arm64")
        val args = command.buildArgs()
        assertTrue(args.contains("myrepo/myimage:amd64"))
        assertTrue(args.contains("myrepo/myimage:arm64"))
    }

    @Test
    fun `ManifestCreateCommand buildArgs with amend`() {
        val command = ManifestCreateCommand("myrepo/myimage:latest")
            .manifest("myrepo/myimage:amd64")
            .amend()
        val args = command.buildArgs()
        assertTrue(args.contains("--amend"))
    }

    @Test
    fun `ManifestCreateCommand buildArgs with insecure`() {
        val command = ManifestCreateCommand("myrepo/myimage:latest")
            .manifest("myrepo/myimage:amd64")
            .insecure()
        val args = command.buildArgs()
        assertTrue(args.contains("--insecure"))
    }

    // ManifestAnnotateCommand tests

    @Test
    fun `ManifestAnnotateCommand buildArgs basic`() {
        val command = ManifestAnnotateCommand("myrepo/myimage:latest", "myrepo/myimage:arm64")
        val args = command.buildArgs()
        assertEquals("manifest", args[0])
        assertEquals("annotate", args[1])
        assertTrue(args.contains("myrepo/myimage:latest"))
        assertTrue(args.contains("myrepo/myimage:arm64"))
    }

    @Test
    fun `ManifestAnnotateCommand buildArgs with arch and os`() {
        val command = ManifestAnnotateCommand("myrepo/myimage:latest", "myrepo/myimage:arm64")
            .arch("arm64")
            .os("linux")
        val args = command.buildArgs()
        assertTrue(args.contains("--arch"))
        assertTrue(args.contains("arm64"))
        assertTrue(args.contains("--os"))
        assertTrue(args.contains("linux"))
    }

    @Test
    fun `ManifestAnnotateCommand buildArgs with variant`() {
        val command = ManifestAnnotateCommand("myrepo/myimage:latest", "myrepo/myimage:arm64")
            .arch("arm64")
            .variant("v8")
        val args = command.buildArgs()
        assertTrue(args.contains("--variant"))
        assertTrue(args.contains("v8"))
    }

    @Test
    fun `ManifestAnnotateCommand buildArgs with os version and features`() {
        val command = ManifestAnnotateCommand("myrepo/myimage:latest", "myrepo/myimage:windows")
            .os("windows")
            .osVersion("10.0.17763")
            .osFeature("win32k")
        val args = command.buildArgs()
        assertTrue(args.contains("--os-version"))
        assertTrue(args.contains("10.0.17763"))
        assertTrue(args.contains("--os-features"))
        assertTrue(args.contains("win32k"))
    }

    // ManifestInspectCommand tests

    @Test
    fun `ManifestInspectCommand buildArgs basic`() {
        val command = ManifestInspectCommand("myrepo/myimage:latest")
        val args = command.buildArgs()
        assertEquals(listOf("manifest", "inspect", "myrepo/myimage:latest"), args)
    }

    @Test
    fun `ManifestInspectCommand buildArgs with verbose`() {
        val command = ManifestInspectCommand("myrepo/myimage:latest")
            .verbose()
        val args = command.buildArgs()
        assertTrue(args.contains("--verbose"))
    }

    @Test
    fun `ManifestInspectCommand buildArgs with insecure`() {
        val command = ManifestInspectCommand("myrepo/myimage:latest")
            .insecure()
        val args = command.buildArgs()
        assertTrue(args.contains("--insecure"))
    }

    @Test
    fun `ManifestInspectCommand buildArgs with manifest list`() {
        val command = ManifestInspectCommand("myrepo/myimage:arm64")
            .manifestList("myrepo/myimage:latest")
        val args = command.buildArgs()
        assertTrue(args.contains("myrepo/myimage:latest"))
        assertTrue(args.contains("myrepo/myimage:arm64"))
    }

    // ManifestPushCommand tests

    @Test
    fun `ManifestPushCommand buildArgs basic`() {
        val command = ManifestPushCommand("myrepo/myimage:latest")
        assertEquals(listOf("manifest", "push", "myrepo/myimage:latest"), command.buildArgs())
    }

    @Test
    fun `ManifestPushCommand buildArgs with purge`() {
        val command = ManifestPushCommand("myrepo/myimage:latest")
            .purge()
        val args = command.buildArgs()
        assertTrue(args.contains("--purge"))
    }

    @Test
    fun `ManifestPushCommand buildArgs with insecure`() {
        val command = ManifestPushCommand("myrepo/myimage:latest")
            .insecure()
        val args = command.buildArgs()
        assertTrue(args.contains("--insecure"))
    }

    @Test
    fun `ManifestPushCommand buildArgs with all options`() {
        val command = ManifestPushCommand("myrepo/myimage:latest")
            .insecure()
            .purge()
        val args = command.buildArgs()
        assertTrue(args.contains("--insecure"))
        assertTrue(args.contains("--purge"))
    }

    // ManifestRmCommand tests

    @Test
    fun `ManifestRmCommand buildArgs single`() {
        val command = ManifestRmCommand()
            .manifestList("myrepo/myimage:latest")
        val args = command.buildArgs()
        assertEquals(listOf("manifest", "rm", "myrepo/myimage:latest"), args)
    }

    @Test
    fun `ManifestRmCommand buildArgs multiple`() {
        val command = ManifestRmCommand()
            .manifestLists("myrepo/myimage:v1", "myrepo/myimage:v2")
        val args = command.buildArgs()
        assertTrue(args.contains("myrepo/myimage:v1"))
        assertTrue(args.contains("myrepo/myimage:v2"))
    }

    @Test
    fun `ManifestRmCommand builder with manifest list`() {
        val command = ManifestRmCommand.builder("myrepo/myimage:latest")
        val args = command.buildArgs()
        assertTrue(args.contains("myrepo/myimage:latest"))
    }
}
