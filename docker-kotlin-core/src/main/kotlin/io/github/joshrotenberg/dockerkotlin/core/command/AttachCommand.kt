package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to attach local standard input, output, and error streams to a running container.
 *
 * Equivalent to `docker attach`.
 *
 * Example usage:
 * ```kotlin
 * AttachCommand("my-container").execute()
 * AttachCommand("my-container").noStdin().execute()
 * ```
 */
class AttachCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var detachKeys: String? = null
    private var noStdin: Boolean = false
    private var sigProxy: Boolean = true

    /** Override the key sequence for detaching a container. */
    fun detachKeys(keys: String) = apply { this.detachKeys = keys }

    /** Do not attach STDIN. */
    fun noStdin() = apply { this.noStdin = true }

    /** Proxy all received signals to the process (default true). */
    fun sigProxy(proxy: Boolean) = apply { this.sigProxy = proxy }

    override fun buildArgs(): List<String> = buildList {
        add("attach")
        detachKeys?.let { add("--detach-keys"); add(it) }
        if (noStdin) add("--no-stdin")
        if (!sigProxy) add("--sig-proxy=false")
        add(container)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): AttachCommand = AttachCommand(container)
    }
}
