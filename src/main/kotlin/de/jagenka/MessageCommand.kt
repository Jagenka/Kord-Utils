package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent

interface MessageCommand
{
    /**
     * Prefix for command literal, so that message is considered a command
     */
    val prefix: String

    /**
     * This represents the literals by which the command is identified
     */
    val names: List<String>

    /**
     * Short help text shown in command overview
     */
    val shortHelpText: String

    /**
     * Long help text shown for detailed help
     */
    val longHelpText: String

    //TODO: implement help command

    /**
     * This represents, if the command needs admin powers.
     */
    val needsAdmin: Boolean

    /**
     * This represents, if the command needs to be executed in a channel marked NSFW.
     */
    val needsNSFW: Boolean

    /**
     * This method will be called immediately after registering it with a MessageCommandRegistry.
     */
    fun prepare(kord: Kord)

    /**
     * This method will be run when message starts with cmd sequence + this.name
     * @param event MessageCreateEvent from which this method is called
     * @param args is the message word by word (split by " ")
     */
    suspend fun execute(event: MessageCreateEvent, args: List<String>)
}

val MessageCommand.firstWords: List<String>
    get() = names.map { "$prefix$it" }

val MessageCommand.commandExample: String
    get() = "${prefix}${names.firstOrNull()}"