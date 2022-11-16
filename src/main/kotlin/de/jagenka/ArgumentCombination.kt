package de.jagenka

import dev.kord.core.event.message.MessageCreateEvent

class ArgumentCombination(private val arguments: List<Argument>, private val executes: suspend (event: MessageCreateEvent, args: List<String>) -> Boolean) // TODO?: unlimited args
{
    private fun fitsTo(args: List<String>): Boolean
    {
        if (args.size != arguments.size) return false

        arguments.forEachIndexed { index, argument ->
            if (!argument.hasFittingType(args[index])) return false
        }

        return true
    }

    internal suspend fun runIfFitting(event: MessageCreateEvent, args: List<String>): Boolean
    {
        if (!fitsTo(args.drop(1))) return false

        return executes.invoke(event, args)
    }
}