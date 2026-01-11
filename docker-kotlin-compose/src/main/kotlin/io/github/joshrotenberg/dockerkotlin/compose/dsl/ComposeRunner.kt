package io.github.joshrotenberg.dockerkotlin.compose.dsl

import io.github.joshrotenberg.dockerkotlin.compose.ComposeDownCommand
import io.github.joshrotenberg.dockerkotlin.compose.ComposePsCommand
import io.github.joshrotenberg.dockerkotlin.compose.ComposeUpCommand
import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import java.io.File
import java.nio.file.Files

/**
 * Runner for executing Docker Compose operations from a DSL specification.
 *
 * Example usage:
 * ```kotlin
 * val compose = dockerCompose {
 *     service("web") {
 *         image = "nginx:alpine"
 *         ports("8080:80")
 *     }
 * }
 *
 * // Run the compose stack
 * compose.runner()
 *     .projectName("my-app")
 *     .up()
 *     .execute()
 *
 * // Or use the extension function
 * compose.up {
 *     detach()
 *     build()
 * }
 * ```
 */
class ComposeRunner(
    private val spec: ComposeSpec,
    private val executor: CommandExecutor = CommandExecutor()
) {
    private var projectName: String? = null
    private var projectDir: File? = null
    private var composeFile: File? = null
    private var keepFile: Boolean = false

    /**
     * Set the project name.
     */
    fun projectName(name: String) = apply { this.projectName = name }

    /**
     * Set the project directory.
     */
    fun projectDir(dir: File) = apply { this.projectDir = dir }

    /**
     * Set the project directory.
     */
    fun projectDir(path: String) = projectDir(File(path))

    /**
     * Use an existing compose file instead of generating one.
     */
    fun composeFile(file: File) = apply { this.composeFile = file }

    /**
     * Keep the generated compose file after execution.
     */
    fun keepFile(keep: Boolean = true) = apply { this.keepFile = keep }

    /**
     * Get or create the compose file.
     */
    private fun getOrCreateComposeFile(): File {
        composeFile?.let { return it }

        val dir = projectDir ?: Files.createTempDirectory("docker-compose-").toFile()
        val file = File(dir, "docker-compose.yml")
        spec.writeTo(file)

        if (!keepFile) {
            file.deleteOnExit()
            if (projectDir == null) {
                dir.deleteOnExit()
            }
        }

        composeFile = file
        return file
    }

    /**
     * Create an "up" command.
     */
    fun up(): ComposeUpCommand {
        val file = getOrCreateComposeFile()
        val cmd = ComposeUpCommand(executor)
        cmd.file(file.absolutePath)
        projectName?.let { cmd.projectName(it) }
        return cmd
    }

    /**
     * Create a "down" command.
     */
    fun down(): ComposeDownCommand {
        val file = getOrCreateComposeFile()
        val cmd = ComposeDownCommand(executor)
        cmd.file(file.absolutePath)
        projectName?.let { cmd.projectName(it) }
        return cmd
    }

    /**
     * Create a "ps" command.
     */
    fun ps(): ComposePsCommand {
        val file = getOrCreateComposeFile()
        val cmd = ComposePsCommand(executor)
        cmd.file(file.absolutePath)
        projectName?.let { cmd.projectName(it) }
        return cmd
    }

    /**
     * Write the compose file without executing.
     */
    fun writeFile(path: String): File {
        val file = File(path)
        spec.writeTo(file)
        return file
    }

    /**
     * Write the compose file without executing.
     */
    fun writeFile(file: File): File {
        spec.writeTo(file)
        return file
    }
}

// Extension functions for ComposeSpec

/**
 * Create a runner for this compose specification.
 */
fun ComposeSpec.runner(executor: CommandExecutor = CommandExecutor()): ComposeRunner {
    return ComposeRunner(this, executor)
}

/**
 * Run "docker compose up" with this specification.
 */
suspend fun ComposeSpec.up(
    projectName: String? = null,
    init: ComposeUpCommand.() -> Unit = {}
) {
    runner()
        .apply { projectName?.let { projectName(it) } }
        .up()
        .apply(init)
        .execute()
}

/**
 * Run "docker compose up" with this specification (blocking).
 */
fun ComposeSpec.upBlocking(
    projectName: String? = null,
    init: ComposeUpCommand.() -> Unit = {}
) {
    runner()
        .apply { projectName?.let { projectName(it) } }
        .up()
        .apply(init)
        .executeBlocking()
}

/**
 * Run "docker compose down" with this specification.
 */
suspend fun ComposeSpec.down(
    projectName: String? = null,
    init: ComposeDownCommand.() -> Unit = {}
) {
    runner()
        .apply { projectName?.let { projectName(it) } }
        .down()
        .apply(init)
        .execute()
}

/**
 * Run "docker compose down" with this specification (blocking).
 */
fun ComposeSpec.downBlocking(
    projectName: String? = null,
    init: ComposeDownCommand.() -> Unit = {}
) {
    runner()
        .apply { projectName?.let { projectName(it) } }
        .down()
        .apply(init)
        .executeBlocking()
}

/**
 * Use the compose stack with automatic cleanup.
 *
 * Example:
 * ```kotlin
 * dockerCompose {
 *     service("redis") { image = "redis:7" }
 * }.use("my-app") {
 *     // Stack is running
 *     // Do something with it
 * }
 * // Stack is automatically stopped
 * ```
 */
inline fun ComposeSpec.use(
    projectName: String? = null,
    detach: Boolean = true,
    block: (ComposeRunner) -> Unit
) {
    val runner = runner().apply { projectName?.let { projectName(it) } }

    try {
        runner.up().apply { if (detach) detach() }.executeBlocking()
        block(runner)
    } finally {
        runner.down().volumes().executeBlocking()
    }
}
