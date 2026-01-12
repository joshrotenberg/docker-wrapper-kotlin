package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.CommandOutput
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import java.nio.file.Path
import kotlin.time.Duration

/**
 * Base configuration for Docker Compose commands.
 */
open class ComposeConfig {
    /** Compose file paths (-f, --file). */
    val files = mutableListOf<Path>()

    /** Project name (-p, --project-name). */
    var projectName: String? = null

    /** Project directory (--project-directory). */
    var projectDirectory: Path? = null

    /** Profiles to enable (--profile). */
    val profiles = mutableListOf<String>()

    /** Environment file (--env-file). */
    var envFile: Path? = null

    /** Run in compatibility mode. */
    var compatibility: Boolean = false

    /** Execute in dry run mode. */
    var dryRun: Boolean = false

    /** Progress output type. */
    var progress: ProgressType? = null

    /**
     * Build global compose arguments.
     */
    fun buildGlobalArgs(): List<String> = buildList {
        files.forEach { add("--file"); add(it.toString()) }
        projectName?.let { add("--project-name"); add(it) }
        projectDirectory?.let { add("--project-directory"); add(it.toString()) }
        profiles.forEach { add("--profile"); add(it) }
        envFile?.let { add("--env-file"); add(it.toString()) }
        if (compatibility) add("--compatibility")
        if (dryRun) add("--dry-run")
        progress?.let { add("--progress"); add(it.value) }
    }
}

/**
 * Progress output type for compose commands.
 */
enum class ProgressType(val value: String) {
    AUTO("auto"),
    TTY("tty"),
    PLAIN("plain"),
    JSON("json"),
    QUIET("quiet")
}

/**
 * Base class for Docker Compose commands.
 *
 * Uses self-referential generic to enable fluent API method chaining
 * that returns the concrete subclass type.
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractComposeCommand<T, Self : AbstractComposeCommand<T, Self>>(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<T>(executor) {

    protected val config = ComposeConfig()

    /** Add a compose file. */
    fun file(path: Path): Self = apply { config.files.add(path) } as Self

    /** Add a compose file by string path. */
    fun file(path: String): Self = apply { config.files.add(Path.of(path)) } as Self

    /** Set the project name. */
    fun projectName(name: String): Self = apply { config.projectName = name } as Self

    /** Set the project directory. */
    fun projectDirectory(path: Path): Self = apply { config.projectDirectory = path } as Self

    /** Set the project directory by string path. */
    fun projectDirectory(path: String): Self = apply { config.projectDirectory = Path.of(path) } as Self

    /** Add a profile. */
    fun profile(profile: String): Self = apply { config.profiles.add(profile) } as Self

    /** Set the environment file. */
    fun envFile(path: Path): Self = apply { config.envFile = path } as Self

    /** Set the environment file by string path. */
    fun envFile(path: String): Self = apply { config.envFile = Path.of(path) } as Self

    /** Enable compatibility mode. */
    fun compatibility(): Self = apply { config.compatibility = true } as Self

    /** Enable dry run mode. */
    fun dryRun(): Self = apply { config.dryRun = true } as Self

    /** Set progress output type. */
    fun progress(progress: ProgressType): Self = apply { config.progress = progress } as Self

    /**
     * Get the compose subcommand (e.g., "up", "down").
     */
    protected abstract fun subcommand(): String

    /**
     * Build subcommand-specific arguments.
     */
    protected abstract fun buildSubcommandArgs(): List<String>

    override fun buildArgs(): List<String> = buildList {
        add("compose")
        addAll(config.buildGlobalArgs())
        add(subcommand())
        addAll(buildSubcommandArgs())
    }

    /**
     * Execute the compose command and get raw output.
     */
    override suspend fun executeRaw(): CommandOutput {
        val args = buildArgs() + rawArgs
        val output = executor.execute(args, commandTimeout)

        if (!output.success) {
            throw DockerException.CommandFailed(
                command = "docker ${args.joinToString(" ")}",
                exitCode = output.exitCode,
                stdout = output.stdout,
                stderr = output.stderr
            )
        }

        return output
    }

    /**
     * Execute the compose command synchronously and get raw output.
     */
    override fun executeRawBlocking(): CommandOutput {
        val args = buildArgs() + rawArgs
        val output = executor.executeBlocking(args, commandTimeout)

        if (!output.success) {
            throw DockerException.CommandFailed(
                command = "docker ${args.joinToString(" ")}",
                exitCode = output.exitCode,
                stdout = output.stdout,
                stderr = output.stderr
            )
        }

        return output
    }
}
