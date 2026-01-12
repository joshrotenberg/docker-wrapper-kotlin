package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.CommandOutput
import io.github.joshrotenberg.dockerkotlin.core.StreamHandle
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import io.github.joshrotenberg.dockerkotlin.core.retry.RetryExecutor
import io.github.joshrotenberg.dockerkotlin.core.retry.RetryPolicy
import kotlinx.coroutines.flow.Flow
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

    /**
     * Execute the command asynchronously with retry support.
     *
     * @param policy The retry policy to use
     * @return The command result
     * @throws DockerException if all retries are exhausted
     */
    suspend fun executeWithRetry(policy: RetryPolicy = RetryPolicy.DEFAULT): T

    /**
     * Execute the command synchronously with retry support (blocking).
     *
     * @param policy The retry policy to use
     * @return The command result
     * @throws DockerException if all retries are exhausted
     */
    fun executeBlockingWithRetry(policy: RetryPolicy = RetryPolicy.DEFAULT): T
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
 * Interface for commands that support streaming output.
 *
 * Commands implementing this interface can return output line-by-line
 * as it becomes available, rather than waiting for completion.
 */
interface StreamingDockerCommand<T> : DockerCommand<T> {
    /**
     * Execute the command with streaming output.
     *
     * Returns a StreamHandle that provides access to output as it becomes
     * available. The caller is responsible for closing the handle.
     *
     * Example usage (Kotlin):
     * ```kotlin
     * command.stream().use { handle ->
     *     handle.asFlow().collect { line ->
     *         println(line)
     *     }
     * }
     * ```
     *
     * Example usage (Java):
     * ```java
     * try (StreamHandle handle = command.stream()) {
     *     for (String line : handle) {
     *         System.out.println(line);
     *     }
     * }
     * ```
     *
     * @return StreamHandle for reading output
     */
    fun stream(): StreamHandle

    /**
     * Execute the command and return output as a Kotlin Flow.
     *
     * This is a convenience method that wraps stream().asFlow().
     * The underlying process is automatically cleaned up when the
     * flow collection completes or is cancelled.
     *
     * @return Flow of output lines
     */
    fun asFlow(): Flow<String>
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

    override suspend fun executeWithRetry(policy: RetryPolicy): T {
        return RetryExecutor.executeWithRetry(policy) { execute() }
    }

    override fun executeBlockingWithRetry(policy: RetryPolicy): T {
        return RetryExecutor.executeWithRetryBlocking(policy) { executeBlocking() }
    }
}

/**
 * Abstract base class for Docker commands that support streaming output.
 */
abstract class AbstractStreamingDockerCommand<T>(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<T>(executor), StreamingDockerCommand<T> {

    override fun stream(): StreamHandle {
        val args = buildArgs() + rawArgs
        return executor.executeStreaming(args)
    }

    override fun asFlow(): Flow<String> {
        val handle = stream()
        return handle.asFlow()
    }
}
