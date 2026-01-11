package io.github.joshrotenberg.dockerkotlin.core

import io.github.joshrotenberg.dockerkotlin.core.error.DockerException
import io.github.joshrotenberg.dockerkotlin.core.platform.PlatformInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
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

        val result = withTimeoutOrNull(effectiveTimeout.inWholeMilliseconds) {
            executeProcess(runtime, args)
        }

        if (result == null) {
            throw DockerException.Timeout(effectiveTimeout)
        }

        result
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

        val completed = process.waitFor(effectiveTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        if (!completed) {
            process.destroyForcibly()
            throw DockerException.Timeout(effectiveTimeout)
        }

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.exitValue()

        logger.debug("Command completed with exit code {}", exitCode)

        return CommandOutput(stdout, stderr, exitCode)
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

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        logger.debug("Command completed with exit code {}", exitCode)

        CommandOutput(stdout, stderr, exitCode)
    }
}
