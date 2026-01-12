package io.github.joshrotenberg.dockerkotlin.core.command.context

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContextCommandsTest {

    // ContextCreateCommand tests

    @Test
    fun `ContextCreateCommand buildArgs basic`() {
        val command = ContextCreateCommand("my-context")
        assertEquals(listOf("context", "create", "my-context"), command.buildArgs())
    }

    @Test
    fun `ContextCreateCommand buildArgs with description`() {
        val command = ContextCreateCommand("my-context")
            .description("My remote host")
        val args = command.buildArgs()
        assertTrue(args.contains("--description"))
        assertTrue(args.contains("My remote host"))
        assertTrue(args.contains("my-context"))
    }

    @Test
    fun `ContextCreateCommand buildArgs with from`() {
        val command = ContextCreateCommand("new-context")
            .from("existing-context")
        val args = command.buildArgs()
        assertTrue(args.contains("--from"))
        assertTrue(args.contains("existing-context"))
    }

    @Test
    fun `ContextCreateCommand buildArgs with docker options`() {
        val command = ContextCreateCommand("remote-context")
            .dockerHost("tcp://myserver:2376")
            .ca("~/ca-file")
            .cert("~/cert-file")
            .key("~/key-file")
        val args = command.buildArgs()
        assertTrue(args.contains("--docker"))
        val dockerArg = args[args.indexOf("--docker") + 1]
        assertTrue(dockerArg.contains("host=tcp://myserver:2376"))
        assertTrue(dockerArg.contains("ca=~/ca-file"))
        assertTrue(dockerArg.contains("cert=~/cert-file"))
        assertTrue(dockerArg.contains("key=~/key-file"))
    }

    @Test
    fun `ContextCreateCommand buildArgs with skipTlsVerify`() {
        val command = ContextCreateCommand("insecure-context")
            .dockerHost("tcp://myserver:2375")
            .skipTlsVerify()
        val args = command.buildArgs()
        val dockerArg = args[args.indexOf("--docker") + 1]
        assertTrue(dockerArg.contains("skip-tls-verify=true"))
    }

    // ContextInspectCommand tests

    @Test
    fun `ContextInspectCommand buildArgs basic`() {
        val command = ContextInspectCommand()
            .context("my-context")
        val args = command.buildArgs()
        assertEquals(listOf("context", "inspect", "my-context"), args)
    }

    @Test
    fun `ContextInspectCommand buildArgs multiple contexts`() {
        val command = ContextInspectCommand()
            .contexts("context1", "context2")
        val args = command.buildArgs()
        assertTrue(args.contains("context1"))
        assertTrue(args.contains("context2"))
    }

    @Test
    fun `ContextInspectCommand buildArgs with format`() {
        val command = ContextInspectCommand()
            .context("my-context")
            .format("json")
        val args = command.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("json"))
    }

    // ContextLsCommand tests

    @Test
    fun `ContextLsCommand buildArgs basic`() {
        val command = ContextLsCommand()
        assertEquals(listOf("context", "ls"), command.buildArgs())
    }

    @Test
    fun `ContextLsCommand buildArgs with quiet`() {
        val command = ContextLsCommand().quiet()
        val args = command.buildArgs()
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `ContextLsCommand buildArgs with format`() {
        val command = ContextLsCommand()
            .format("{{.Name}}")
        val args = command.buildArgs()
        assertTrue(args.contains("--format"))
        assertTrue(args.contains("{{.Name}}"))
    }

    // ContextRmCommand tests

    @Test
    fun `ContextRmCommand buildArgs basic`() {
        val command = ContextRmCommand()
            .context("old-context")
        val args = command.buildArgs()
        assertEquals(listOf("context", "rm", "old-context"), args)
    }

    @Test
    fun `ContextRmCommand buildArgs multiple contexts`() {
        val command = ContextRmCommand()
            .contexts("ctx1", "ctx2", "ctx3")
        val args = command.buildArgs()
        assertTrue(args.contains("ctx1"))
        assertTrue(args.contains("ctx2"))
        assertTrue(args.contains("ctx3"))
    }

    @Test
    fun `ContextRmCommand buildArgs with force`() {
        val command = ContextRmCommand()
            .context("active-context")
            .force()
        val args = command.buildArgs()
        assertTrue(args.contains("--force"))
    }

    // ContextUpdateCommand tests

    @Test
    fun `ContextUpdateCommand buildArgs basic`() {
        val command = ContextUpdateCommand("my-context")
        assertEquals(listOf("context", "update", "my-context"), command.buildArgs())
    }

    @Test
    fun `ContextUpdateCommand buildArgs with description`() {
        val command = ContextUpdateCommand("my-context")
            .description("Updated description")
        val args = command.buildArgs()
        assertTrue(args.contains("--description"))
        assertTrue(args.contains("Updated description"))
    }

    @Test
    fun `ContextUpdateCommand buildArgs with docker options`() {
        val command = ContextUpdateCommand("my-context")
            .dockerHost("tcp://newserver:2376")
        val args = command.buildArgs()
        assertTrue(args.contains("--docker"))
        val dockerArg = args[args.indexOf("--docker") + 1]
        assertTrue(dockerArg.contains("host=tcp://newserver:2376"))
    }

    // ContextUseCommand tests

    @Test
    fun `ContextUseCommand buildArgs`() {
        val command = ContextUseCommand("my-context")
        assertEquals(listOf("context", "use", "my-context"), command.buildArgs())
    }

    // ContextShowCommand tests

    @Test
    fun `ContextShowCommand buildArgs`() {
        val command = ContextShowCommand()
        assertEquals(listOf("context", "show"), command.buildArgs())
    }

    // ContextExportCommand tests

    @Test
    fun `ContextExportCommand buildArgs basic`() {
        val command = ContextExportCommand("my-context")
        assertEquals(listOf("context", "export", "my-context"), command.buildArgs())
    }

    @Test
    fun `ContextExportCommand buildArgs with output`() {
        val command = ContextExportCommand("my-context")
            .output("my-context.tar")
        val args = command.buildArgs()
        assertTrue(args.contains("my-context.tar"))
    }

    // ContextImportCommand tests

    @Test
    fun `ContextImportCommand buildArgs`() {
        val command = ContextImportCommand("new-context", "context.tar")
        assertEquals(listOf("context", "import", "new-context", "context.tar"), command.buildArgs())
    }
}
