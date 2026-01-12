package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to build or rebuild services.
 *
 * Equivalent to `docker compose build`.
 *
 * Example usage:
 * ```kotlin
 * ComposeBuildCommand()
 *     .file("docker-compose.yml")
 *     .noCache()
 *     .services("web", "api")
 *     .execute()
 * ```
 */
class ComposeBuildCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeBuildCommand>(executor) {

    private var noCache = false
    private var pull = false
    private var push = false
    private var quiet = false
    private var withDependencies = false
    private val buildArgs = mutableMapOf<String, String>()
    private val services = mutableListOf<String>()
    private var memory: String? = null
    private var sshKeys: String? = null

    /** Do not use cache when building the image. */
    fun noCache() = apply { noCache = true }

    /** Always attempt to pull a newer version of the image. */
    fun pull() = apply { pull = true }

    /** Push service images after building. */
    fun push() = apply { push = true }

    /** Don't print anything to STDOUT. */
    fun quiet() = apply { quiet = true }

    /** Also build dependencies. */
    fun withDependencies() = apply { withDependencies = true }

    /** Set build-time variables. */
    fun buildArg(name: String, value: String) = apply { buildArgs[name] = value }

    /** Set memory limit for the build container. */
    fun memory(limit: String) = apply { memory = limit }

    /** Set SSH authentications for build. */
    fun ssh(keys: String) = apply { sshKeys = keys }

    /** Specify services to build. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "build"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (noCache) add("--no-cache")
        if (pull) add("--pull")
        if (push) add("--push")
        if (quiet) add("--quiet")
        if (withDependencies) add("--with-dependencies")
        buildArgs.forEach { (name, value) ->
            add("--build-arg")
            add("$name=$value")
        }
        memory?.let { add("--memory"); add(it) }
        sshKeys?.let { add("--ssh"); add(it) }
        addAll(services)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): ComposeBuildCommand = ComposeBuildCommand()
    }
}
