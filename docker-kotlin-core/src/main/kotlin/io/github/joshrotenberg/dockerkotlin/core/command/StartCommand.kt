package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to start one or more stopped containers.
 *
 * Equivalent to `docker start`.
 *
 * Example usage:
 * ```kotlin
 * StartCommand("my-container").execute()
 * ```
 */
class StartCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    private var attach: Boolean = false
    private var interactive: Boolean = false
    private var detachKeys: String? = null
    private var checkpoint: String? = null
    private var checkpointDir: String? = null

    /** Attach STDOUT/STDERR and forward signals. */
    fun attach() = apply { this.attach = true }

    /** Attach container's STDIN. */
    fun interactive() = apply { this.interactive = true }

    /** Override the key sequence for detaching a container. */
    fun detachKeys(keys: String) = apply { this.detachKeys = keys }

    /** Restore from this checkpoint. */
    fun checkpoint(name: String) = apply { this.checkpoint = name }

    /** Use a custom checkpoint storage directory. */
    fun checkpointDir(dir: String) = apply { this.checkpointDir = dir }

    override fun buildArgs(): List<String> = buildList {
        add("start")
        if (attach) add("--attach")
        if (interactive) add("--interactive")
        detachKeys?.let { add("--detach-keys"); add(it) }
        checkpoint?.let { add("--checkpoint"); add(it) }
        checkpointDir?.let { add("--checkpoint-dir"); add(it) }
        addAll(containers)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): StartCommand = StartCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): StartCommand = StartCommand(containers)
    }
}
