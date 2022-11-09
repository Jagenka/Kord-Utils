package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

/**
 * Registry for all MessageCommands belonging to a Kord instance. The bot needs to have message read access, otherwise this will not work.
 */
class MessageCommandRegistry(val kord: Kord)
{
    private val commands = mutableSetOf<MessageCommand>()

    init
    {
        kord.on<MessageCreateEvent> {
            // return if author is a bot or undefined
            if (message.author?.isBot != false) return@on


        }
    }

    fun register(command: MessageCommand)
    {
        commands.add(command)
        command.prepare(kord)
    }
}