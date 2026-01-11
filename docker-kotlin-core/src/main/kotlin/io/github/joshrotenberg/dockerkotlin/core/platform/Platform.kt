package io.github.joshrotenberg.dockerkotlin.core.platform

/**
 * Operating system platform.
 */
enum class Platform {
    LINUX,
    MACOS,
    WINDOWS,
    UNKNOWN;

    companion object {
        /**
         * Detect the current platform.
         */
        fun detect(): Platform {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("linux") -> LINUX
                osName.contains("mac") || osName.contains("darwin") -> MACOS
                osName.contains("win") -> WINDOWS
                else -> UNKNOWN
            }
        }
    }
}
