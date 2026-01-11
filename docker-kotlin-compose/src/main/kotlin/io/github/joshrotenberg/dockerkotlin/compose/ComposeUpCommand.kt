package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to start compose services.
 *
 * Equivalent to `docker compose up`.
 *
 * Example usage:
 * ```kotlin
 * ComposeUpCommand()
 *     .file("docker-compose.yml")
 *     .projectName("myapp")
 *     .detach()
 *     .build()
 *     .execute()
 * ```
 */
class ComposeUpCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit>(executor) {

    private var detached = false
    private var buildImages = false
    private var forceRecreate = false
    private var noRecreate = false
    private var noBuild = false
    private var noStart = false
    private var removeOrphans = false
    private var wait = false
    private var timeout: Int? = null
    private val services = mutableListOf<String>()
    private var scale: MutableMap<String, Int> = mutableMapOf()

    /** Run in detached mode. */
    fun detach() = apply { detached = true }

    /** Build images before starting containers. */
    fun build() = apply { buildImages = true }

    /** Recreate containers even if configuration hasn't changed. */
    fun forceRecreate() = apply { forceRecreate = true }

    /** Don't recreate containers. */
    fun noRecreate() = apply { noRecreate = true }

    /** Don't build images. */
    fun noBuild() = apply { noBuild = true }

    /** Don't start services. */
    fun noStart() = apply { noStart = true }

    /** Remove containers for services not defined in the Compose file. */
    fun removeOrphans() = apply { removeOrphans = true }

    /** Wait for services to be healthy. */
    fun wait() = apply { wait = true }

    /** Shutdown timeout in seconds. */
    fun timeout(seconds: Int) = apply { timeout = seconds }

    /** Specify services to start. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    /** Scale a service to N instances. */
    fun scale(service: String, replicas: Int) = apply { scale[service] = replicas }

    override fun subcommand(): String = "up"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (detached) add("--detach")
        if (buildImages) add("--build")
        if (forceRecreate) add("--force-recreate")
        if (noRecreate) add("--no-recreate")
        if (noBuild) add("--no-build")
        if (noStart) add("--no-start")
        if (removeOrphans) add("--remove-orphans")
        if (wait) add("--wait")
        timeout?.let { add("--timeout"); add(it.toString()) }
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
        fun builder(): ComposeUpCommand = ComposeUpCommand()
    }
}
