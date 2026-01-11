package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Represents a port mapping.
 */
data class PortMapping(
    val containerPort: Int,
    val protocol: String,
    val hostIp: String,
    val hostPort: Int
)

/**
 * Command to list port mappings or a specific mapping for the container.
 *
 * Equivalent to `docker port`.
 *
 * Example usage:
 * ```kotlin
 * val ports = PortCommand("my-container").execute()
 * val port = PortCommand("my-container").privatePort(80).execute()
 * ```
 */
class PortCommand(
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<List<PortMapping>>(executor) {

    private var privatePort: Int? = null
    private var protocol: String? = null

    /** The private port to look up. */
    fun privatePort(port: Int) = apply { this.privatePort = port }

    /** The protocol (tcp or udp). */
    fun protocol(protocol: String) = apply { this.protocol = protocol }

    override fun buildArgs(): List<String> = buildList {
        add("port")
        add(container)
        privatePort?.let { port ->
            val portSpec = if (protocol != null) "$port/$protocol" else port.toString()
            add(portSpec)
        }
    }

    override suspend fun execute(): List<PortMapping> {
        return parseOutput(executeRaw().stdout)
    }

    override fun executeBlocking(): List<PortMapping> {
        return parseOutput(executeRawBlocking().stdout)
    }

    private fun parseOutput(stdout: String): List<PortMapping> {
        return stdout.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> parsePortLine(line) }
    }

    private fun parsePortLine(line: String): PortMapping? {
        // Format: 80/tcp -> 0.0.0.0:8080
        val regex = Regex("""(\d+)/(\w+)\s*->\s*([^:]+):(\d+)""")
        val match = regex.find(line) ?: return null
        return PortMapping(
            containerPort = match.groupValues[1].toInt(),
            protocol = match.groupValues[2],
            hostIp = match.groupValues[3],
            hostPort = match.groupValues[4].toInt()
        )
    }

    companion object {
        @JvmStatic
        fun builder(container: String): PortCommand = PortCommand(container)
    }
}
