package io.github.joshrotenberg.dockerkotlin.core.command

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for image command argument building.
 */
class ImageCommandsTest {

    @Test
    fun `ImagesCommand buildArgs basic`() {
        val cmd = ImagesCommand()
        assertEquals(listOf("images"), cmd.buildArgs())
    }

    @Test
    fun `ImagesCommand buildArgs with repository`() {
        val cmd = ImagesCommand("nginx")
        val args = cmd.buildArgs()
        assertTrue(args.contains("nginx"))
    }

    @Test
    fun `ImagesCommand buildArgs with options`() {
        val cmd = ImagesCommand()
            .all()
            .digests()
            .filterDangling()
            .noTrunc()
            .quiet()

        val args = cmd.buildArgs()
        assertTrue(args.contains("--all"))
        assertTrue(args.contains("--digests"))
        assertTrue(args.contains("--filter"))
        assertTrue(args.contains("dangling=true"))
        assertTrue(args.contains("--no-trunc"))
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `BuildCommand buildArgs basic`() {
        val cmd = BuildCommand(".")
        assertEquals(listOf("build", "."), cmd.buildArgs())
    }

    @Test
    fun `BuildCommand buildArgs with options`() {
        val cmd = BuildCommand("./app")
            .tag("my-image:latest")
            .tag("my-image:v1")
            .file("Dockerfile.prod")
            .buildArg("VERSION", "1.0")
            .label("maintainer", "dev@example.com")
            .target("production")
            .noCache()
            .pull()

        val args = cmd.buildArgs()
        assertEquals("build", args[0])
        assertTrue(args.contains("--tag"))
        assertTrue(args.contains("my-image:latest"))
        assertTrue(args.contains("my-image:v1"))
        assertTrue(args.contains("--file"))
        assertTrue(args.contains("Dockerfile.prod"))
        assertTrue(args.contains("--build-arg"))
        assertTrue(args.contains("VERSION=1.0"))
        assertTrue(args.contains("--label"))
        assertTrue(args.contains("maintainer=dev@example.com"))
        assertTrue(args.contains("--target"))
        assertTrue(args.contains("production"))
        assertTrue(args.contains("--no-cache"))
        assertTrue(args.contains("--pull"))
        assertTrue(args.last() == "./app")
    }

    @Test
    fun `HistoryCommand buildArgs`() {
        val cmd = HistoryCommand("nginx:latest")
            .noTrunc()
            .quiet()

        val args = cmd.buildArgs()
        assertEquals("history", args[0])
        assertTrue(args.contains("--no-trunc"))
        assertTrue(args.contains("--quiet"))
        assertTrue(args.contains("nginx:latest"))
    }

    @Test
    fun `RmiCommand buildArgs`() {
        val cmd = RmiCommand(listOf("image1", "image2"))
            .force()
            .noPrune()

        val args = cmd.buildArgs()
        assertEquals("rmi", args[0])
        assertTrue(args.contains("--force"))
        assertTrue(args.contains("--no-prune"))
        assertTrue(args.contains("image1"))
        assertTrue(args.contains("image2"))
    }

    @Test
    fun `TagCommand buildArgs`() {
        val cmd = TagCommand("source:latest", "target:v1")
        assertEquals(listOf("tag", "source:latest", "target:v1"), cmd.buildArgs())
    }

    @Test
    fun `PushCommand buildArgs`() {
        val cmd = PushCommand("registry.example.com/my-image:v1")
            .allTags()
            .quiet()

        val args = cmd.buildArgs()
        assertEquals("push", args[0])
        assertTrue(args.contains("--all-tags"))
        assertTrue(args.contains("--quiet"))
        assertTrue(args.contains("registry.example.com/my-image:v1"))
    }

    @Test
    fun `SaveCommand buildArgs`() {
        val cmd = SaveCommand(listOf("image1", "image2"))
            .output("/tmp/images.tar")

        val args = cmd.buildArgs()
        assertEquals("save", args[0])
        assertTrue(args.contains("--output"))
        assertTrue(args.contains("/tmp/images.tar"))
        assertTrue(args.contains("image1"))
        assertTrue(args.contains("image2"))
    }

    @Test
    fun `LoadCommand buildArgs`() {
        val cmd = LoadCommand()
            .input("/tmp/image.tar")
            .quiet()

        val args = cmd.buildArgs()
        assertEquals("load", args[0])
        assertTrue(args.contains("--input"))
        assertTrue(args.contains("/tmp/image.tar"))
        assertTrue(args.contains("--quiet"))
    }

    @Test
    fun `ImportCommand buildArgs`() {
        val cmd = ImportCommand("/tmp/container.tar", "my-image:imported")
            .change("ENV DEBUG=1")
            .message("Imported from backup")

        val args = cmd.buildArgs()
        assertEquals("import", args[0])
        assertTrue(args.contains("--change"))
        assertTrue(args.contains("ENV DEBUG=1"))
        assertTrue(args.contains("--message"))
        assertTrue(args.contains("Imported from backup"))
        assertTrue(args.contains("/tmp/container.tar"))
        assertTrue(args.contains("my-image:imported"))
    }

    @Test
    fun `SearchCommand buildArgs`() {
        val cmd = SearchCommand("nginx")
            .stars(100)
            .official()
            .limit(25)
            .noTrunc()

        val args = cmd.buildArgs()
        assertEquals("search", args[0])
        assertTrue(args.contains("--filter"))
        assertTrue(args.contains("stars=100"))
        assertTrue(args.contains("is-official=true"))
        assertTrue(args.contains("--limit"))
        assertTrue(args.contains("25"))
        assertTrue(args.contains("--no-trunc"))
        assertTrue(args.contains("nginx"))
    }

    @Test
    fun `PullCommand buildArgs`() {
        val cmd = PullCommand("nginx:alpine")
            .allTags()
            .platform("linux/amd64")

        val args = cmd.buildArgs()
        assertEquals("pull", args[0])
        assertTrue(args.contains("--all-tags"))
        assertTrue(args.contains("--platform"))
        assertTrue(args.contains("linux/amd64"))
        assertTrue(args.contains("nginx:alpine"))
    }
}
