package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent

abstract class MessageCommand : Comparable<MessageCommand>
{
    internal var registry: MessageCommandRegistry? = null

    /**
     * This represents the literals by which the command is identified
     */
    abstract val ids: List<String>

    /**
     * This represents, if the command needs to be executed in a channel marked NSFW.
     */
    abstract val needsNSFW: Boolean

    /**
     * Short help text shown in command overview
     */
    abstract val helpText: String

    /**
     * This method will be called immediately after registering it with a MessageCommandRegistry.
     */
    open fun prepare(kord: Kord) = Unit

    abstract val allowedArgumentCombinations: List<ArgumentCombination>

    internal suspend fun run(event: MessageCreateEvent, args: List<String>)
    {
        allowedArgumentCombinations.sorted().forEach { combination ->
            if (combination.fitsTo(args.drop(1)))
            {
                if (combination.needsAdmin && registry?.isSenderAdmin?.invoke(event) != true)
                {
                    registry?.needsAdminResponse?.invoke(event)
                    return
                }
                combination.run(event, args)
                return
            }
        }
    }

    override fun compareTo(other: MessageCommand): Int
    {
        return (ids.firstOrNull() ?: "").compareTo(other.ids.firstOrNull() ?: "")
    }
}