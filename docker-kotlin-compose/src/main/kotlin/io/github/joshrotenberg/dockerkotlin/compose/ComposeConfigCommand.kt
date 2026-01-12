package io.github.joshrotenberg.dockerkotlin.compose

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Output format for compose config.
 */
enum class ConfigFormat(val value: String) {
    YAML("yaml"),
    JSON("json")
}

/**
 * Command to validate and view the Compose file.
 *
 * Equivalent to `docker compose config`.
 *
 * Example usage:
 * ```kotlin
 * val config = ComposeConfigCommand()
 *     .file("docker-compose.yml")
 *     .format(ConfigFormat.JSON)
 *     .execute()
 * ```
 */
class ComposeConfigCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractComposeCommand<String, ComposeConfigCommand>(executor) {

    private var format: ConfigFormat? = null
    private var hash: String? = null
    private var images = false
    private var noConsistency = false
    private var noInterpolate = false
    private var noNormalize = false
    private var noPathResolution = false
    private var output: String? = null
    private var profiles = false
    private var quiet = false
    private var resolveImageDigests = false
    private var services = false
    private var variables = false
    private var volumes = false

    /** Format the output (yaml or json). */
    fun format(format: ConfigFormat) = apply { this.format = format }

    /** Print the service config hash. */
    fun hash(service: String) = apply { hash = service }

    /** Print the image names. */
    fun images() = apply { images = true }

    /** Don't check model consistency. */
    fun noConsistency() = apply { noConsistency = true }

    /** Don't interpolate environment variables. */
    fun noInterpolate() = apply { noInterpolate = true }

    /** Don't normalize compose model. */
    fun noNormalize() = apply { noNormalize = true }

    /** Don't resolve file paths. */
    fun noPathResolution() = apply { noPathResolution = true }

    /** Save to file instead of stdout. */
    fun output(path: String) = apply { output = path }

    /** Print the profile names. */
    fun profiles() = apply { profiles = true }

    /** Only validate the configuration. */
    fun quiet() = apply { quiet = true }

    /** Pin image tags to digests. */
    fun resolveImageDigests() = apply { resolveImageDigests = true }

    /** Print the service names. */
    fun services() = apply { services = true }

    /** Print the variables. */
    fun variables() = apply { variables = true }

    /** Print the volume names. */
    fun volumes() = apply { volumes = true }

    override fun subcommand(): String = "config"

    override fun buildSubcommandArgs(): List<String> = buildList {
        format?.let { add("--format"); add(it.value) }
        hash?.let { add("--hash"); add(it) }
        if (images) add("--images")
        if (noConsistency) add("--no-consistency")
        if (noInterpolate) add("--no-interpolate")
        if (noNormalize) add("--no-normalize")
        if (noPathResolution) add("--no-path-resolution")
        output?.let { add("--output"); add(it) }
        if (profiles) add("--profiles")
        if (quiet) add("--quiet")
        if (resolveImageDigests) add("--resolve-image-digests")
        if (services) add("--services")
        if (variables) add("--variables")
        if (volumes) add("--volumes")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): ComposeConfigCommand = ComposeConfigCommand()
    }
}
