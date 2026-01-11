package io.github.joshrotenberg.dockerkotlin.core.command.builder

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand
import io.github.joshrotenberg.dockerkotlin.core.error.DockerException

/**
 * Command to create a new builder instance.
 *
 * Equivalent to `docker builder create` / `docker buildx create`.
 *
 * Example usage:
 * ```kotlin
 * BuilderCreateCommand()
 *     .name("my-builder")
 *     .driver("docker-container")
 *     .use()
 *     .executeBlocking()
 * ```
 */
class BuilderCreateCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var name: String? = null
    private var driver: String? = null
    private val driverOpts = mutableMapOf<String, String>()
    private var node: String? = null
    private var platform: String? = null
    private var useBuilder = false
    private var bootstrap = false
    private var append = false
    private var buildkitdFlags: String? = null
    private var configFile: String? = null
    private var leaveBuildkit = false

    /** Set the builder name. */
    fun name(name: String) = apply { this.name = name }

    /** Set the driver to use (docker, docker-container, kubernetes, remote). */
    fun driver(driver: String) = apply { this.driver = driver }

    /** Add a driver option. */
    fun driverOpt(key: String, value: String) = apply { driverOpts[key] = value }

    /** Set the node name. */
    fun node(node: String) = apply { this.node = node }

    /** Set the platform(s) to build for (e.g., "linux/amd64,linux/arm64"). */
    fun platform(platform: String) = apply { this.platform = platform }

    /** Set the builder as the current builder. */
    fun use() = apply { useBuilder = true }

    /** Bootstrap the builder after creation. */
    fun bootstrap() = apply { bootstrap = true }

    /** Append a node to an existing builder. */
    fun append() = apply { append = true }

    /** Set flags for buildkitd. */
    fun buildkitdFlags(flags: String) = apply { buildkitdFlags = flags }

    /** Set the buildkit config file. */
    fun config(file: String) = apply { configFile = file }

    /** Leave buildkit running after exiting. */
    fun leaveBuildkit() = apply { leaveBuildkit = true }

    override fun buildArgs(): List<String> = buildList {
        add("buildx")
        add("create")

        name?.let { add("--name"); add(it) }
        driver?.let { add("--driver"); add(it) }

        driverOpts.forEach { (key, value) ->
            add("--driver-opt")
            add("$key=$value")
        }

        node?.let { add("--node"); add(it) }
        platform?.let { add("--platform"); add(it) }

        if (useBuilder) add("--use")
        if (bootstrap) add("--bootstrap")
        if (append) add("--append")

        buildkitdFlags?.let { add("--buildkitd-flags"); add(it) }
        configFile?.let { add("--config"); add(it) }

        if (leaveBuildkit) add("--leave")
    }

    override suspend fun execute(): String {
        checkRuntimeSupport()
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        checkRuntimeSupport()
        return executeRawBlocking().stdout.trim()
    }

    private fun checkRuntimeSupport() {
        if (!executor.supportsBuilderCommand("create")) {
            throw DockerException.UnsupportedByRuntime(
                command = "builder create",
                runtime = executor.runtime?.name ?: "unknown"
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): BuilderCreateCommand = BuilderCreateCommand()
    }
}
