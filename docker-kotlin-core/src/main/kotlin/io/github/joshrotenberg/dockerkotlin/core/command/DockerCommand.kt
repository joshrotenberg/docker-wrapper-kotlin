package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.CommandOutput
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import kotlin.time.Duration

/**
 * Base interface for all Docker commands.
 *
 * @param T The return type of the command execution
 */
interface DockerCommand<T> {
    /**
     * Build the command arguments (without the 'docker' prefix).
     */
    fun buildArgs(): List<String>

    /**
     * Execute the command asynchronously.
     *
     * @return The command result
     * @throws DockerException if the command fails
     */
    suspend fun execute(): T

    /**
     * Execute the command synchronously (blocking).
     *
     * @return The command result
     * @throws DockerException if the command fails
     */
    fun executeBlocking(): T

    /**
     * Add a raw argument to the command (escape hatch for unmapped options).
     *
     * @param arg The argument to add
     * @return This command for chaining
     */
    fun arg(arg: String): DockerCommand<T>

    /**
     * Add multiple raw arguments to the command (escape hatch for unmapped options).
     *
     * @param args The arguments to add
     * @return This command for chaining
     */
    fun args(vararg args: String): DockerCommand<T>

    /**
     * Set a timeout for command execution.
     *
     * @param duration The timeout duration
     * @return This command for chaining
     */
    fun timeout(duration: Duration): DockerCommand<T>

    /**
     * Preview the command that would be executed (for debugging/dry-run).
     *
     * @return A preview of the command
     */
    fun preview(): CommandPreview
}

/**
 * Preview of a command that would be executed.
 */
data class CommandPreview(
    /** The full command line that would be executed. */
    val commandLine: String,
    /** The individual arguments. */
    val args: List<String>
) {
    override fun toString(): String = commandLine
}

/**
 * Abstract base class for Docker commands with common functionality.
 */
abstract class AbstractDockerCommand<T>(
    protected val executor: CommandExecutor = CommandExecutor()
) : DockerCommand<T> {

    protected val rawArgs = mutableListOf<String>()
    protected var commandTimeout: Duration? = null

    override fun arg(arg: String): DockerCommand<T> {
        rawArgs.add(arg)
        @Suppress("UNCHECKED_CAST")
        return this as DockerCommand<T>
    }

    override fun args(vararg args: String): DockerCommand<T> {
        rawArgs.addAll(args)
        @Suppress("UNCHECKED_CAST")
        return this as DockerCommand<T>
    }

    override fun timeout(duration: Duration): DockerCommand<T> {
        commandTimeout = duration
        @Suppress("UNCHECKED_CAST")
        return this as DockerCommand<T>
    }

    override fun preview(): CommandPreview {
        val args = buildArgs() + rawArgs
        return CommandPreview(
            commandLine = "docker ${args.joinToString(" ")}",
            args = args
        )
    }

    /**
     * Execute the command and get raw output.
     */
    protected open suspend fun executeRaw(): CommandOutput {
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
     * Execute the command synchronously and get raw output.
     */
    protected open fun executeRawBlocking(): CommandOutput {
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
