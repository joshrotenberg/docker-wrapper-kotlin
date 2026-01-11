package io.github.joshrotenberg.dockerkotlin.core

/**
 * Output from executing a Docker command.
 */
data class CommandOutput(
    /** Standard output from the command. */
    val stdout: String,
    /** Standard error from the command. */
    val stderr: String,
    /** Exit code from the command. */
    val exitCode: Int
) {
    /** Whether the command was successful (exit code 0). */
    val success: Boolean get() = exitCode == 0

    /** Get stdout as a list of lines. */
    fun stdoutLines(): List<String> = stdout.lines().filter { it.isNotBlank() }

    /** Get stderr as a list of lines. */
    fun stderrLines(): List<String> = stderr.lines().filter { it.isNotBlank() }

    /** Check if stdout is empty or blank. */
    fun stdoutIsEmpty(): Boolean = stdout.isBlank()

    /** Check if stderr is empty or blank. */
    fun stderrIsEmpty(): Boolean = stderr.isBlank()
}
