package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to push service images.
 *
 * Equivalent to `docker compose push`.
 *
 * Example usage:
 * ```kotlin
 * ComposePushCommand()
 *     .file("docker-compose.yml")
 *     .ignorePushFailures()
 *     .services("web")
 *     .execute()
 * ```
 */
class ComposePushCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<Unit, ComposePushCommand>(executor) {

    private var ignorePushFailures = false
    private var includeDeps = false
    private var quiet = false
    private val services = mutableListOf<String>()

    /** Push what it can and ignore images with push failures. */
    fun ignorePushFailures() = apply { ignorePushFailures = true }

    /** Also push images of services declared as dependencies. */
    fun includeDeps() = apply { includeDeps = true }

    /** Push without printing progress information. */
    fun quiet() = apply { quiet = true }

    /** Specify services to push. */
    fun services(vararg services: String) = apply { this.services.addAll(services) }

    override fun subcommand(): String = "push"

    override fun buildSubcommandArgs(): List<String> = buildList {
        if (ignorePushFailures) add("--ignore-push-failures")
        if (includeDeps) add("--include-deps")
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
        fun builder(): ComposePushCommand = ComposePushCommand()
    }
}
