package de.jagenka

//TODO: arguments in help text
//TODO: noch nicht angepasst
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
    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(emptyList()) { event, _ ->
                    Util.sendMessageInSameChannel(event, registry.getShortHelpTexts().joinToString(separator = "\n"))
                    true
                }
        )
}