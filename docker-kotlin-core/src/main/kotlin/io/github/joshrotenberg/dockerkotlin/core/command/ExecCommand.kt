package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Output from docker exec command.
 */
data class ExecOutput(
    /** Standard output from the command. */
    val stdout: String,
    /** Standard error from the command. */
    val stderr: String,
    /** Exit code of the executed command. */
    val exitCode: Int
) {
    /** Check if the command executed successfully. */
    val success: Boolean get() = exitCode == 0

    /** Get combined output (stdout + stderr). */
    fun combinedOutput(): String = when {
        stderr.isEmpty() -> stdout
        stdout.isEmpty() -> stderr
        else -> "$stdout\n$stderr"
    }
}

/**
 * Command to execute a command in a running container.
 *
 * Equivalent to `docker exec`.
 *
 * Example usage:
 * ```kotlin
 * val output = ExecCommand("my-container", "ls", "-la").execute()
 * val output = ExecCommand("my-container", listOf("ls", "-la"))
 *     .workdir("/app")
 *     .user("root")
 *     .execute()
 * ```
 */
class ExecCommand(
    private val container: String,
    private val command: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<ExecOutput>(executor) {

    constructor(container: String, vararg command: String, executor: CommandExecutor = CommandExecutor()) :
            this(container, command.toList(), executor)

    private var detach: Boolean = false
    private var detachKeys: String? = null
    private val envVars = mutableMapOf<String, String>()
    private val envFiles = mutableListOf<String>()
    private var interactive: Boolean = false
    private var privileged: Boolean = false
    private var tty: Boolean = false
    private var user: String? = null
    private var workdir: String? = null

    /** Run in detached mode (background). */
    fun detach() = apply { this.detach = true }

    /** Override the key sequence for detaching a container. */
    fun detachKeys(keys: String) = apply { this.detachKeys = keys }

    /** Add an environment variable. */
    fun env(key: String, value: String) = apply { this.envVars[key] = value }

    /** Add multiple environment variables. */
    fun envs(vars: Map<String, String>) = apply { this.envVars.putAll(vars) }

    /** Add an environment file. */
    fun envFile(file: String) = apply { this.envFiles.add(file) }

    /** Keep STDIN open even if not attached. */
    fun interactive() = apply { this.interactive = true }

    /** Give extended privileges to the command. */
    fun privileged() = apply { this.privileged = true }

    /** Allocate a pseudo-TTY. */
    fun tty() = apply { this.tty = true }

    /** Username or UID (format: "<name|uid>[:<group|gid>]"). */
    fun user(user: String) = apply { this.user = user }

    /** Working directory inside the container. */
    fun workdir(workdir: String) = apply { this.workdir = workdir }

    /** Convenience method for interactive TTY mode (-it). */
    fun it() = apply { interactive(); tty() }

    override fun buildArgs(): List<String> = buildList {
        add("exec")
        if (detach) add("--detach")
        detachKeys?.let { add("--detach-keys"); add(it) }
        envVars.forEach { (key, value) -> add("--env"); add("$key=$value") }
        envFiles.forEach { add("--env-file"); add(it) }
        if (interactive) add("--interactive")
        if (privileged) add("--privileged")
        if (tty) add("--tty")
        user?.let { add("--user"); add(it) }
        workdir?.let { add("--workdir"); add(it) }
        add(container)
        addAll(command)
    }

    override suspend fun execute(): ExecOutput {
        val output = executeRaw()
        return ExecOutput(
            stdout = output.stdout,
            stderr = output.stderr,
            exitCode = output.exitCode
        )
    }

    override fun executeBlocking(): ExecOutput {
        val output = executeRawBlocking()
        return ExecOutput(
            stdout = output.stdout,
            stderr = output.stderr,
            exitCode = output.exitCode
        )
    }

    companion object {
        @JvmStatic
        fun builder(container: String, command: List<String>): ExecCommand =
            ExecCommand(container, command)

        @JvmStatic
        fun builder(container: String, vararg command: String): ExecCommand =
            ExecCommand(container, command.toList())
    }
}
