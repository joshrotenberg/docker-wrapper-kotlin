package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a Docker volume.
 *
 * Equivalent to `docker volume create`.
 *
 * Example usage:
 * ```kotlin
 * VolumeCreateCommand()
 *     .name("my-volume")
 *     .driver("local")
 *     .executeBlocking()
 * ```
 */
class VolumeCreateCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var name: String? = null
    private var driver: String? = null
    private val driverOpts = mutableMapOf<String, String>()
    private val labels = mutableMapOf<String, String>()

    /** Set volume name. */
    fun name(name: String) = apply { this.name = name }

    /** Set the volume driver. */
    fun driver(driver: String) = apply { this.driver = driver }

    /** Add a driver option. */
    fun driverOpt(key: String, value: String) = apply { driverOpts[key] = value }

    /** Add a label. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("create")

        driver?.let { add("--driver"); add(it) }

        driverOpts.forEach { (key, value) ->
            add("--opt")
            add("$key=$value")
        }

        labels.forEach { (key, value) ->
            add("--label")
            add("$key=$value")
        }

        name?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(): VolumeCreateCommand = VolumeCreateCommand()
    }
}
