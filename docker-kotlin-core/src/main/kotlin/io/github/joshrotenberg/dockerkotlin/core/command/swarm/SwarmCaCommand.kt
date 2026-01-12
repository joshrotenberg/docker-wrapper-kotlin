package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display and rotate the root CA.
 *
 * Equivalent to `docker swarm ca`.
 *
 * Example usage:
 * ```kotlin
 * // Display the current CA certificate
 * val caCert = SwarmCaCommand()
 *     .execute()
 *
 * // Rotate the CA certificate
 * SwarmCaCommand()
 *     .rotate()
 *     .certExpiry("8760h")
 *     .execute()
 * ```
 */
class SwarmCaCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var caCert: String? = null
    private var caKey: String? = null
    private var certExpiry: String? = null
    private var detach = false
    private var externalCa: String? = null
    private var quiet = false
    private var rotate = false

    /** Path to the PEM-formatted root CA certificate to use. */
    fun caCert(path: String) = apply { caCert = path }

    /** Path to the PEM-formatted root CA key to use. */
    fun caKey(path: String) = apply { caKey = path }

    /** Validity period for node certificates (e.g., "2160h0m0s"). */
    fun certExpiry(expiry: String) = apply { certExpiry = expiry }

    /** Exit immediately instead of waiting for CA rotation to finish. */
    fun detach() = apply { detach = true }

    /** Specifications of one or more certificate signing endpoints. */
    fun externalCa(ca: String) = apply { externalCa = ca }

    /** Suppress progress output. */
    fun quiet() = apply { quiet = true }

    /** Rotate the swarm CA. */
    fun rotate() = apply { rotate = true }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("ca")
        caCert?.let { add("--ca-cert"); add(it) }
        caKey?.let { add("--ca-key"); add(it) }
        certExpiry?.let { add("--cert-expiry"); add(it) }
        if (detach) add("--detach")
        externalCa?.let { add("--external-ca"); add(it) }
        if (quiet) add("--quiet")
        if (rotate) add("--rotate")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmCaCommand = SwarmCaCommand()
    }
}
