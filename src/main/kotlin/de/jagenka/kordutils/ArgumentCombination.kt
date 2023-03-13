package de.jagenka.kordutils

import dev.kord.core.event.message.MessageCreateEvent

class ArgumentCombination(
        internal val arguments: List<Argument<*>>,// TODO?: vararg
        internal val helpText: String,
        internal val needsAdmin: Boolean = false,
        private val executes: suspend (event: MessageCreateEvent, arguments: Map<String, Any?>) -> Boolean
) : Comparable<ArgumentCombination>
{
    internal fun fitsTo(args: List<String>): Boolean
    {
        if (args.size != arguments.size) return false

        arguments.forEachIndexed { index, argument ->
            if (!argument.isOfType(args[index])) return false
        }

        return true
    }

    internal suspend fun run(event: MessageCreateEvent, args: List<String>): Boolean
    {
        val map = mutableMapOf<String, Any?>()
        args.drop(1).forEachIndexed { index, arg ->
            val argument = arguments[index]
            map[argument.id] = argument.convertToType(arg)
        }
        return executes.invoke(event, map.toMap())
    }

    private val rank: Int
        get()
        {
            return -arguments.count { it is LiteralArgument }
        }

    override fun compareTo(other: ArgumentCombination): Int
    {
        return rank.compareTo(other.rank)
    }
}