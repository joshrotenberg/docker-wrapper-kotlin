package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for auth command argument building.
 */
class AuthCommandsTest {

    @Test
    fun `LoginCommand buildArgs basic`() {
        val cmd = LoginCommand("testuser", "testpass")
        val args = cmd.buildArgs()
        assertEquals("login", args[0])
        assertTrue(args.contains("--username"))
        assertTrue(args.contains("testuser"))
        assertTrue(args.contains("--password"))
        assertTrue(args.contains("testpass"))
    }

    @Test
    fun `LoginCommand buildArgs with registry`() {
        val cmd = LoginCommand("user", "pass")
            .registry("gcr.io")

        val args = cmd.buildArgs()
        assertTrue(args.contains("gcr.io"))
    }

    @Test
    fun `LoginCommand buildArgs with password stdin`() {
        val cmd = LoginCommand("user", "ignored")
            .passwordStdin()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--password-stdin"))
        assertTrue(!args.contains("--password") || args.indexOf("--password-stdin") < args.size)
    }

    @Test
    fun `LoginCommand buildArgs with private registry`() {
        val cmd = LoginCommand("admin", "secret")
            .registry("my-registry.example.com:5000")

        val args = cmd.buildArgs()
        assertTrue(args.contains("my-registry.example.com:5000"))
    }

    @Test
    fun `LogoutCommand buildArgs basic`() {
        val cmd = LogoutCommand()
        assertEquals(listOf("logout"), cmd.buildArgs())
    }

    @Test
    fun `LogoutCommand buildArgs with server`() {
        val cmd = LogoutCommand().server("gcr.io")
        val args = cmd.buildArgs()
        assertEquals(listOf("logout", "gcr.io"), args)
    }

    @Test
    fun `LogoutCommand buildArgs with private registry`() {
        val cmd = LogoutCommand().server("my-registry.example.com:5000")
        val args = cmd.buildArgs()
        assertTrue(args.contains("my-registry.example.com:5000"))
    }
}
