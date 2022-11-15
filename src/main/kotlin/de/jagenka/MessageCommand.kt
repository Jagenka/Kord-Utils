package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent

abstract class MessageCommand
{
    val subcommands = mutableListOf<MessageCommand>()

    val firstWords: List<String>
        get() = names.map { "$prefix$it" }

    /**
     * Prefix for command literal, so that message is considered a command
     */
    abstract val prefix: String

    /**
     * This represents the literals by which the command is identified
     */
    abstract val names: List<String>

    /**
     * This represents, if the command needs admin powers.
     */
    abstract val needsAdmin: Boolean

    /**
     * This represents, if the command needs to be executed in a channel marked NSFW.
     */
    abstract val needsNSFW: Boolean

    /**
     * Short help text shown in command overview
     */
    abstract val shortHelpText: String

    /**
     * Long help text shown for detailed help
     */
    abstract val longHelpText: String

    internal fun getPreorderWithPrefix(prefix: String): List<Pair<String, MessageCommand>>
    {
        val result = mutableListOf<Pair<String, MessageCommand>>()
        val newPrefix = "$prefix ${this.prefix}${names.joinToString(separator = "|")}".trim()

        result.add(newPrefix to this)
        subcommands.forEach { result.addAll(it.getPreorderWithPrefix(newPrefix)) }

        return result
    }

    /**
     * This method will be called immediately after registering it with a MessageCommandRegistry.
     */
    open fun prepare(kord: Kord) = Unit

    /**
     * This method determines what this command should do.
     * @param event MessageCreateEvent from which this method is called
     * @param args is the message word by word (split by " ")
     */
    abstract suspend fun execute(event: MessageCreateEvent, args: List<String>) // TODO: commands may only contain subcommands but cannot be executed on their own

    internal suspend fun findSubcommand(level: Int, event: MessageCreateEvent, args: List<String>)
    {
        args.getOrNull(level + 1)?.let { subcommandName ->
            subcommands.forEach { subcommand ->
                if ("${subcommand.prefix}$subcommandName" in subcommand.names)
                {
                    subcommand.findSubcommand(level + 1, event, args)
                    return
                }
            }
        }

        execute(event, args)
    }
}