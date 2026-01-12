package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to pull service images.
 *
 * Equivalent to `docker compose pull`.
 *
 * Example usage:
 * ```kotlin
 * ComposePullCommand()
 *     .file("docker-compose.yml")
 *     .quiet()
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposePullCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposePullCommand>(executor) {

    private var ignoreBuildable = false
    private var ignorePullFailures = false
    private var includeDeps = false
    private var policy: PullPolicy? = null
    private var quiet = false
    private val services = mutableListOf<String>()

    /** Ignore images that can be built. */
    fun ignoreBuildable() = apply { ignoreBuildable = true }

    /** Pull what it can and ignore images with pull failures. */
    fun ignorePullFailures() = apply { ignorePullFailures = true }

    /** Also pull services declared as dependencies. */
    fun includeDeps() = apply { includeDeps = true }

    /** Pull policy (missing, always). */
    fun policy(policy: PullPolicy) = apply { this.policy = policy }

    /** Pull without printing progress information. */
    fun quiet() = apply { quiet = true }

    /** Specify services to pull. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "pull"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (ignoreBuildable) add("--ignore-buildable")
        if (ignorePullFailures) add("--ignore-pull-failures")
        if (includeDeps) add("--include-deps")
        policy?.let { add("--policy"); add(it.value) }
        if (quiet) add("--quiet")
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
        fun builder(): ComposePullCommand = ComposePullCommand()
    }
}
