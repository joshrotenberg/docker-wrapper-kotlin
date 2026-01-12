package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.CommandOutput

/**
 * Command to execute a command in a running container.
 *
 * Equivalent to `docker compose exec`.
 *
 * Example usage:
 * ```kotlin
 * val result = ComposeExecCommand("web")
 *     .file("docker-compose.yml")
 *     .command("ls", "-la")
 *     .execute()
 * ```
 */
class ComposeExecCommand(
    private val service: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<CommandOutput, ComposeExecCommand>(executor) {

    private var detach = false
    private val environment = mutableMapOf<String, String>()
    private var index = 1
    private var interactive = true
    private var noTty = false
    private var privileged = false
    private var user: String? = null
    private var workdir: String? = null
    private val command = mutableListOf<String>()

    /** Run in detached mode. */
    fun detach() = apply { detach = true }

    /** Set environment variable. */
    fun env(name: String, value: String) = apply { environment[name] = value }

    /** Index of the container if service has multiple replicas. */
    fun index(index: Int) = apply { this.index = index }

    /** Keep STDIN open (default true). */
    fun interactive(enabled: Boolean) = apply { interactive = enabled }

    /** Disable pseudo-TTY allocation. */
    fun noTty() = apply { noTty = true }

    /** Give extended privileges to the process. */
    fun privileged() = apply { privileged = true }

    /** Run as specified username or uid. */
    fun user(user: String) = apply { this.user = user }

    /** Working directory inside the container. */
    fun workdir(dir: String) = apply { workdir = dir }

    /** Set the command to execute. */
    fun command(vararg cmd: String) = apply { command.clear(); command.addAll(cmd) }

    override fun subcommand(): String = "exec"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (detach) add("--detach")
        environment.forEach { (name, value) ->
            add("--env")
            add("$name=$value")
        }
        if (index != 1) {
            add("--index"); add(index.toString())
        }
        if (!interactive) add("--no-stdin")
        if (noTty) add("--no-TTY")
        if (privileged) add("--privileged")
        user?.let { add("--user"); add(it) }
        workdir?.let { add("--workdir"); add(it) }
        add(service)
        addAll(command)
    }

    override suspend fun execute(): CommandOutput {
        return executeRaw()
    }

    override fun executeBlocking(): CommandOutput {
        return executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(service: String): ComposeExecCommand = ComposeExecCommand(service)
    }
}
