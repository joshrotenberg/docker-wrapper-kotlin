package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VersionCommandTest {

    @Test
    fun `buildArgs returns version command`() {
        val cmd = VersionCommand()
        assertEquals(listOf("version"), cmd.buildArgs())
    }

    @Test
    fun `parse extracts version info from docker output`() {
        val output = """
            Client:
             Version:           24.0.7
             API version:       1.43
             Go version:        go1.21.3
             Git commit:        afdd53b
             Built:             Thu Oct 26 09:07:41 2023
             OS/Arch:           darwin/arm64
             Context:           orbstack

            Server:
             Engine:
              Version:          24.0.7
              API version:      1.43 (minimum version 1.12)
              Go version:       go1.21.3
              Git commit:       311b9ff
              Built:            Thu Oct 26 09:07:41 2023
              OS/Arch:          linux/arm64
              Experimental:     false
        """.trimIndent()

        val info = VersionInfo.parse(output)

        assertEquals("24.0.7", info.clientVersion)
        assertEquals("24.0.7", info.serverVersion)
        assertEquals("1.43", info.apiVersion)
        assertEquals("go1.21.3", info.goVersion)
        assertEquals("darwin", info.os)
        assertEquals("arm64", info.arch)
    }

    @Test
    fun `parse handles missing server info`() {
        val output = """
            Client:
             Version:           24.0.7
             API version:       1.43
             Go version:        go1.21.3
             OS/Arch:           darwin/arm64
        """.trimIndent()

        val info = VersionInfo.parse(output)

        assertEquals("24.0.7", info.clientVersion)
        assertEquals(null, info.serverVersion)
    }
}
