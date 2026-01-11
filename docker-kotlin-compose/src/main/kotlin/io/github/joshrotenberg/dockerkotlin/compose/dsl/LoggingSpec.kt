package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Logging configuration.
 */
class LoggingSpec(
    /** Logging driver. */
    var driver: String
) {
    /** Driver options. */
    private val opts = mutableMapOf<String, String>()

    /**
     * Add driver option.
     */
    fun option(key: String, value: String) {
        opts[key] = value
    }

    /**
     * Add multiple options.
     */
    fun options(vararg pairs: Pair<String, String>) {
        pairs.forEach { (k, v) -> opts[k] = v }
    }

    // Common logging options

    /** Max size of log file. */
    fun maxSize(size: String) = option("max-size", size)

    /** Max number of log files. */
    fun maxFile(count: Int) = option("max-file", count.toString())

    /** Compress rotated files. */
    fun compress(enable: Boolean = true) = option("compress", enable.toString())

    /** Log tag. */
    fun tag(tag: String) = option("tag", tag)

    fun toMap(): Map<String, Any> = buildMap {
        put("driver", driver)
        if (opts.isNotEmpty()) {
            put("options", opts.toMap())
        }
    }
}
