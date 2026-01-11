package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to log in to a Docker registry.
 *
 * Equivalent to `docker login`.
 *
 * Example usage:
 * ```kotlin
 * LoginCommand("myuser", "mypassword").executeBlocking()
 * LoginCommand("myuser", "mypassword").registry("gcr.io").executeBlocking()
 * ```
 *
 * Note: For security, prefer using `passwordStdin()` in production environments.
 */
class LoginCommand(
    private val username: String,
    private val password: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var registry: String? = null
    private var passwordStdin = false

    /** Set the registry server URL (defaults to Docker Hub). */
    fun registry(registry: String) = apply { this.registry = registry }

    /** Read password from stdin for security. */
    fun passwordStdin() = apply { passwordStdin = true }

    override fun buildArgs(): List<String> = buildList {
        add("login")
        add("--username")
        add(username)

        if (passwordStdin) {
            add("--password-stdin")
        } else {
            add("--password")
            add(password)
        }

        registry?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(username: String, password: String): LoginCommand =
            LoginCommand(username, password)
    }
}
