package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.CommandOutput

/**
 * Command to run a one-off command on a service.
 *
 * Equivalent to `docker compose run`.
 *
 * Example usage:
 * ```kotlin
 * val result = ComposeRunCommand("web")
 *     .file("docker-compose.yml")
 *     .rm()
 *     .command("npm", "test")
 *     .execute()
 * ```
 */
class ComposeRunCommand(
    private val service: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<CommandOutput, ComposeRunCommand>(executor) {

    private var build = false
    private var detach = false
    private var entrypoint: String? = null
    private val environment = mutableMapOf<String, String>()
    private var interactive = true
    private val labels = mutableMapOf<String, String>()
    private var name: String? = null
    private var noDeps = false
    private var noTty = false
    private val publish = mutableListOf<String>()
    private var quietPull = false
    private var rm = false
    private var servicePorts = false
    private var useAliases = false
    private var user: String? = null
    private val volumes = mutableListOf<String>()
    private var workdir: String? = null
    private val command = mutableListOf<String>()

    /** Build image before starting container. */
    fun build() = apply { build = true }

    /** Run container in background. */
    fun detach() = apply { detach = true }

    /** Override the entrypoint. */
    fun entrypoint(entrypoint: String) = apply { this.entrypoint = entrypoint }

    /** Set environment variable. */
    fun env(name: String, value: String) = apply { environment[name] = value }

    /** Keep STDIN open (default true). */
    fun interactive(enabled: Boolean) = apply { interactive = enabled }

    /** Add a label. */
    fun label(name: String, value: String) = apply { labels[name] = value }

    /** Assign a name to the container. */
    fun name(name: String) = apply { this.name = name }

    /** Don't start linked services. */
    fun noDeps() = apply { noDeps = true }

    /** Disable pseudo-TTY. */
    fun noTty() = apply { noTty = true }

    /** Publish a container's port(s) to the host. */
    fun publish(port: String) = apply { publish.add(port) }

    /** Pull without printing progress. */
    fun quietPull() = apply { quietPull = true }

    /** Automatically remove the container when it exits. */
    fun rm() = apply { rm = true }

    /** Run command with the service's ports enabled. */
    fun servicePorts() = apply { servicePorts = true }

    /** Use the service's network aliases. */
    fun useAliases() = apply { useAliases = true }

    /** Run as specified username or uid. */
    fun user(user: String) = apply { this.user = user }

    /** Bind mount a volume. */
    fun volume(volume: String) = apply { volumes.add(volume) }

    /** Working directory inside the container. */
    fun workdir(dir: String) = apply { workdir = dir }

    /** Set the command to run. */
    fun command(vararg cmd: String) = apply { command.clear(); command.addAll(cmd) }

    override fun subcommand(): String = "run"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (build) add("--build")
        if (detach) add("--detach")
        entrypoint?.let { add("--entrypoint"); add(it) }
        environment.forEach { (name, value) ->
            add("--env")
            add("$name=$value")
        }
        if (!interactive) add("--no-stdin")
        labels.forEach { (name, value) ->
            add("--label")
            add("$name=$value")
        }
        name?.let { add("--name"); add(it) }
        if (noDeps) add("--no-deps")
        if (noTty) add("--no-TTY")
        publish.forEach { add("--publish"); add(it) }
        if (quietPull) add("--quiet-pull")
        if (rm) add("--rm")
        if (servicePorts) add("--service-ports")
        if (useAliases) add("--use-aliases")
        user?.let { add("--user"); add(it) }
        volumes.forEach { add("--volume"); add(it) }
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
        fun builder(service: String): ComposeRunCommand = ComposeRunCommand(service)
    }
}
