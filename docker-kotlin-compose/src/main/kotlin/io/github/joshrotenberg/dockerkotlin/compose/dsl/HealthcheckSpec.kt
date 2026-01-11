package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Healthcheck configuration.
 */
class HealthcheckSpec {
    /** Test command. */
    private var testCmd: Any? = null

    /** Interval between checks. */
    var interval: String? = null

    /** Timeout for each check. */
    var timeout: String? = null

    /** Number of retries before unhealthy. */
    var retries: Int? = null

    /** Start period (grace period). */
    var startPeriod: String? = null

    /** Start interval (since Compose 2.22). */
    var startInterval: String? = null

    /** Disable healthcheck. */
    var disable: Boolean? = null

    /**
     * Set test command as a string (shell form).
     */
    fun test(cmd: String) {
        testCmd = cmd
    }

    /**
     * Set test command as a list (exec form).
     */
    fun test(vararg args: String) {
        testCmd = args.toList()
    }

    /**
     * Set test using CMD-SHELL.
     */
    fun testShell(cmd: String) {
        testCmd = listOf("CMD-SHELL", cmd)
    }

    /**
     * Set test using CMD (exec form).
     */
    fun testCmd(vararg args: String) {
        testCmd = listOf("CMD") + args.toList()
    }

    fun toMap(): Map<String, Any> = buildMap {
        if (disable == true) {
            put("disable", true)
        } else {
            testCmd?.let { put("test", it) }
            interval?.let { put("interval", it) }
            timeout?.let { put("timeout", it) }
            retries?.let { put("retries", it) }
            startPeriod?.let { put("start_period", it) }
            startInterval?.let { put("start_interval", it) }
        }
    }
}
