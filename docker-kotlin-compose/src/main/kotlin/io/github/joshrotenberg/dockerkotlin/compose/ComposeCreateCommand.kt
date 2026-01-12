package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to create containers for services.
 *
 * Equivalent to `docker compose create`.
 *
 * Example usage:
 * ```kotlin
 * ComposeCreateCommand()
 *     .file("docker-compose.yml")
 *     .build()
 *     .services("web", "db")
 *     .execute()
 * ```
 */
class ComposeCreateCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposeCreateCommand>(executor) {

    private var buildImages = false
    private var forceRecreate = false
    private var noRecreate = false
    private var noBuild = false
    private var removeOrphans = false
    private var pull: PullPolicy? = null
    private var quietPull = false
    private val scale = mutableMapOf<String, Int>()
    private val services = mutableListOf<String>()

    /** Build images before creating containers. */
    fun build() = apply { buildImages = true }

    /** Recreate containers even if configuration hasn't changed. */
    fun forceRecreate() = apply { forceRecreate = true }

    /** Don't recreate containers. */
    fun noRecreate() = apply { noRecreate = true }

    /** Don't build images. */
    fun noBuild() = apply { noBuild = true }

    /** Remove containers for services not defined in the Compose file. */
    fun removeOrphans() = apply { removeOrphans = true }

    /** Pull image policy. */
    fun pull(policy: PullPolicy) = apply { pull = policy }

    /** Pull without printing progress. */
    fun quietPull() = apply { quietPull = true }

    /** Scale a service to N instances. */
    fun scale(service: String, replicas: Int) = apply { scale[service] = replicas }

    /** Specify services to create. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "create"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (buildImages) add("--build")
        if (forceRecreate) add("--force-recreate")
        if (noRecreate) add("--no-recreate")
        if (noBuild) add("--no-build")
        if (removeOrphans) add("--remove-orphans")
        pull?.let { add("--pull"); add(it.value) }
        if (quietPull) add("--quiet-pull")
        scale.forEach { (service, replicas) ->
            add("--scale")
            add("$service=$replicas")
        }
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
        fun builder(): ComposeCreateCommand = ComposeCreateCommand()
    }
}

/**
 * Pull policy for compose commands.
 */
enum class PullPolicy(val value: String) {
    ALWAYS("always"),
    MISSING("missing"),
    NEVER("never")
}
