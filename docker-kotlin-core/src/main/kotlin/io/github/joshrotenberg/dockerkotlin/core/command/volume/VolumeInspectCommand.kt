package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more volumes.
 *
 * Equivalent to `docker volume inspect`.
 *
 * Example usage:
 * ```kotlin
 * VolumeInspectCommand("my-volume").executeBlocking()
 * VolumeInspectCommand("my-volume").format("{{.Mountpoint}}").executeBlocking()
 * ```
 */
class VolumeInspectCommand : AbstractDockerCommand<String> {

    private val volumes: List<String>
    private var format: String? = null

    constructor(volume: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.volumes = listOf(volume)
    }

    constructor(volumes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.volumes = volumes
    }

    /** Set output format using Go template. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("inspect")
        format?.let { add("--format"); add(it) }
        addAll(volumes)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(volume: String): VolumeInspectCommand = VolumeInspectCommand(volume)

        @JvmStatic
        fun builder(volumes: List<String>): VolumeInspectCommand = VolumeInspectCommand(volumes)
    }
}
