package de.jagenka.kordutils

import dev.kord.core.event.message.MessageCreateEvent

abstract class Behavior
{
    internal fun isEnabled(guildId: ULong, channelId: ULong): Boolean
    {
        this::class.simpleName?.let { className ->
            return Config.isBehaviorEnabled(guildId, channelId, className)
        } ?: return false
    }

    abstract suspend fun run(event: MessageCreateEvent)
}