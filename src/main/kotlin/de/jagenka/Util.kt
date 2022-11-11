package de.jagenka

import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction

object Util
{
    suspend fun sendMessageInSameChannel(event: MessageCreateEvent, content: String)
    {
        event.message.channel.createMessage(content)
    }

    suspend fun addReactionToMessage(message: Message, emoji: DiscordEmoji)
    {
        message.addReaction(emoji.toReaction())
    }
}