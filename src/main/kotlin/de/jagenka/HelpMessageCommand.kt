package de.jagenka

import dev.kord.core.event.message.MessageCreateEvent

//TODO: arguments in help text
class HelpMessageCommand(private val registry: MessageCommandRegistry, private val customPrefix: String) : MessageCommand()
{
    override val prefix: String
        get() = customPrefix
    override val names: List<String>
        get() = listOf("help", "?")
    override val needsAdmin: Boolean
        get() = false
    override val needsNSFW: Boolean
        get() = false
    override val shortHelpText: String
        get() = "Displays this help text."
    override val longHelpText: String
        get() = "Display a help text."

    override suspend fun execute(event: MessageCreateEvent, args: List<String>)
    {
        if (args.size == 1)
        {
            Util.sendMessageInSameChannel(event, registry.getShortHelpTexts().joinToString(separator = "\n"))
        }
    }
}