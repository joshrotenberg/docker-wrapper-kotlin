package io.github.joshrotenberg.dockerkotlin.core

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import io.github.joshrotenberg.dockerkotlin.core.platform.PlatformInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Executes Docker CLI commands.
 */
class CommandExecutor(
    private val platformInfo: PlatformInfo? = null,
    private val defaultTimeout: Duration = DEFAULT_TIMEOUT
) {
    private val logger = LoggerFactory.getLogger(CommandExecutor::class.java)

    /** The detected runtime (Podman, Docker, etc.). */
    val runtime get() = platformInfo?.runtime

    companion object {
        /** Default timeout for command execution. */
        val DEFAULT_TIMEOUT: Duration = 30.seconds
    }

    /**
     * Check if a builder subcommand is supported by the current runtime.
     * @param subcommand The builder subcommand (e.g., "create", "ls", "prune")
     * @return true if supported, false otherwise
     */
    fun supportsBuilderCommand(subcommand: String): Boolean {
        return runtime?.supportsBuilderCommand(subcommand) ?: true
    }

    /**
     * Execute a Docker command asynchronously.
     *
     * @param args Command arguments (without the 'docker' prefix)
     * @param timeout Optional timeout override
     * @return Command output
     * @throws DockerException if the command fails
     */
    suspend fun execute(
        args: List<String>,
        timeout: Duration? = null
    ): CommandOutput = withContext(Dispatchers.IO) {
        val effectiveTimeout = timeout ?: defaultTimeout
        val runtime = platformInfo?.runtime?.command ?: "docker"

        logger.debug("Executing: {} {}", runtime, args.joinToString(" "))

        try {
            withTimeout(effectiveTimeout.inWholeMilliseconds) {
                executeProcess(runtime, args)
            }
        } catch (e: TimeoutCancellationException) {
            // TimeoutCancellationException is caught here after process cleanup in executeProcess
            throw DockerException.Timeout(effectiveTimeout)
        }
    }

    /**
     * Execute a Docker command synchronously (blocking).
     *
     * @param args Command arguments (without the 'docker' prefix)
     * @param timeout Optional timeout override
     * @return Command output
     * @throws DockerException if the command fails
     */
    fun executeBlocking(
        args: List<String>,
        timeout: Duration? = null
    ): CommandOutput {
        val effectiveTimeout = timeout ?: defaultTimeout
        val runtime = platformInfo?.runtime?.command ?: "docker"

        logger.debug("Executing (blocking): {} {}", runtime, args.joinToString(" "))

        val processBuilder = ProcessBuilder(listOf(runtime) + args)
            .redirectErrorStream(false)

        // Set environment variables from platform info
        platformInfo?.environmentVars?.let { envVars ->
            processBuilder.environment().putAll(envVars)
        }

        val process = processBuilder.start()

        // Read stdout and stderr concurrently to avoid deadlock when buffers fill
        val executor = Executors.newFixedThreadPool(2)
        val stdoutFuture: Future<String> = executor.submit<String> {
            process.inputStream.bufferedReader().readText()
        }
        val stderrFuture: Future<String> = executor.submit<String> {
            process.errorStream.bufferedReader().readText()
        }

        try {
            val completed = process.waitFor(effectiveTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            if (!completed) {
                process.destroyForcibly()
                throw DockerException.Timeout(effectiveTimeout)
            }

            val stdout = stdoutFuture.get()
            val stderr = stderrFuture.get()
            val exitCode = process.exitValue()

            logger.debug("Command completed with exit code {}", exitCode)

            return CommandOutput(stdout, stderr, exitCode)
        } finally {
            executor.shutdown()
        }
    }

    private suspend fun executeProcess(
        runtime: String,
        args: List<String>
    ): CommandOutput = withContext(Dispatchers.IO) {
        val processBuilder = ProcessBuilder(listOf(runtime) + args)
            .redirectErrorStream(false)

        // Set environment variables from platform info
        platformInfo?.environmentVars?.let { envVars ->
            processBuilder.environment().putAll(envVars)
        }

        val process = processBuilder.start()

        try {
            // Read stdout and stderr concurrently to avoid deadlock when buffers fill
            val stdoutDeferred = async { process.inputStream.bufferedReader().readText() }
            val stderrDeferred = async { process.errorStream.bufferedReader().readText() }

            val stdout = stdoutDeferred.await()
            val stderr = stderrDeferred.await()
            val exitCode = process.waitFor()

            logger.debug("Command completed with exit code {}", exitCode)

            CommandOutput(stdout, stderr, exitCode)
        } catch (e: CancellationException) {
            // Clean up process on cancellation (including timeout via withTimeoutOrNull)
            process.destroyForcibly()
            process.waitFor() // Wait for process to actually terminate
            throw e
        }
    }

    /**
     * Execute a Docker command with streaming output.
     *
     * Returns a StreamHandle that provides access to the command output
     * as it becomes available. The caller is responsible for closing the
     * handle when done.
     *
     * @param args Command arguments (without the 'docker' prefix)
     * @return StreamHandle for reading output
     */
    fun executeStreaming(args: List<String>): StreamHandle {
        val runtime = platformInfo?.runtime?.command ?: "docker"

        logger.debug("Executing (streaming): {} {}", runtime, args.joinToString(" "))

        val processBuilder = ProcessBuilder(listOf(runtime) + args)
            .redirectErrorStream(false)

        // Set environment variables from platform info
        platformInfo?.environmentVars?.let { envVars ->
            processBuilder.environment().putAll(envVars)
        }

        val process = processBuilder.start()
        return StreamHandle.fromProcess(process)
    }
}
