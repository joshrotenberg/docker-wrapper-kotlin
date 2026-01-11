package io.github.joshrotenberg.dockerkotlin.core

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for the Docker client.
 */
class DockerConfig {
    /** Whether to auto-detect the Docker runtime platform. */
    var detectPlatform: Boolean = true

    /** Default timeout for command execution. */
    var defaultTimeout: Duration = 30.seconds

    /** Enable dry-run mode (commands are logged but not executed). */
    var dryRun: Boolean = false

    /** Enable verbose logging. */
    var verbose: Boolean = false
}
