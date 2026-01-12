package io.github.joshrotenberg.dockerkotlin.core.command.stack

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more stacks.
 *
 * Equivalent to `docker stack rm`.
 *
 * Example usage:
 * ```kotlin
 * StackRmCommand("my-stack").executeBlocking()
 * StackRmCommand(listOf("stack1", "stack2")).executeBlocking()
 * ```
 */
class StackRmCommand : AbstractDockerCommand<String> {

    private val stacks: List<String>
    private var detach = true

    constructor(stack: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.stacks = listOf(stack)
    }

    constructor(stacks: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.stacks = stacks
    }

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    override fun buildArgs(): List<String> = buildList {
        add("stack")
        add("rm")
        if (detach) add("--detach")
        addAll(stacks)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(stack: String): StackRmCommand = StackRmCommand(stack)

        @JvmStatic
        fun builder(stacks: List<String>): StackRmCommand = StackRmCommand(stacks)
    }
}
