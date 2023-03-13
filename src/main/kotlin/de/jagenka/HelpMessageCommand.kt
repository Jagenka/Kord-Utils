package de.jagenka

class HelpMessageCommand(private val registry: MessageCommandRegistry) : MessageCommand()
{
    override val ids: List<String>
        get() = listOf("help", "?")
    override val needsAdmin: Boolean
        get() = false
    override val needsNSFW: Boolean
        get() = false
    override val helpText: String
        get() = "Get help for commands."
    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(emptyList(), "Displays this help text.") { event, _ ->
                    Util.sendMessageInSameChannel(event, registry.getShortHelpTexts().joinToString(separator = System.lineSeparator()))
                    true
                },
                ArgumentCombination(listOf(Argument.string("command")), "Get help for a specific command.") { event, arguments ->
                    Util.sendMessageInSameChannel(event, registry.getHelpTextsForCommand(arguments["command"].toString()).joinToString(separator = System.lineSeparator()))
                    true
                }
        )
}