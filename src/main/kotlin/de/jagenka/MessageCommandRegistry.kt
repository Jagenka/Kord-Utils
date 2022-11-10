package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

/**
 * Registry for all MessageCommands belonging to a Kord instance. The bot needs to have message read access, otherwise this will not work.
 */
class MessageCommandRegistry(val kord: Kord)
{
    private val commands = mutableMapOf<String, MessageCommand>()

    init
    {
        kord.on<MessageCreateEvent> {
            // return if author is a bot or undefined
            if (message.author?.isBot != false) return@on

            val args = this.message.content.split(" ")
            val firstWord = args.getOrNull(0) ?: return@on
            commands[firstWord]?.execute(this, args) ?: return@on //TODO: admin and nsfw check
        }
    }

    fun register(command: MessageCommand)
    {
        command.firstWords.forEach { commands[it] = command }
        command.prepare(kord)
    }
}