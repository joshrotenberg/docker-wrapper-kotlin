package io.github.joshrotenberg.dockerkotlin.core.command.volume

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more Docker volumes.
 *
 * Equivalent to `docker volume rm`.
 *
 * Example usage:
 * ```kotlin
 * VolumeRmCommand("my-volume").executeBlocking()
 * VolumeRmCommand(listOf("vol1", "vol2")).force().executeBlocking()
 * ```
 */
class VolumeRmCommand : AbstractDockerCommand<String> {

    private val volumes: List<String>
    private var force = false

    constructor(volume: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.volumes = listOf(volume)
    }

    constructor(volumes: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.volumes = volumes
    }

    /** Force removal. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("volume")
        add("rm")
        if (force) add("--force")
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
        fun builder(volume: String): VolumeRmCommand = VolumeRmCommand(volume)

        @JvmStatic
        fun builder(volumes: List<String>): VolumeRmCommand = VolumeRmCommand(volumes)
    }
}
