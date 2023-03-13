package de.jagenka.kordutils

import de.jagenka.kordutils.Argument.Companion.literal
import de.jagenka.kordutils.Argument.Companion.string
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.runBlocking

object Test
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        val token = System.getenv()["BOT_TOKEN"] ?: error("error reading bot token")

        runBlocking {
            val kord = Kord(token)

            val registry = MessageCommandRegistry(kord, "!")
            registry.register(HelloCommand)
            registry.register(HelpMessageCommand)

//            registry.register(NSFWCommand)
            registry.register(AdminCommand)

            registry.needsNSFWResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.x)
            }

            registry.needsAdminResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.facePalm)
            }

            kord.login {
                @OptIn(PrivilegedIntent::class)
                intents += Intent.MessageContent
            }
        }
    }
}

object HelloCommand : MessageCommand()
{
    override val ids: List<String>
        get() = listOf("hello", "hi")
    override val helpText: String
        get() = "sag hallo jetzt"
    override val needsNSFW: Boolean
        get() = false

    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(emptyList(), "aus nichts kommt nichts") { event, _ ->
                    event.message.channel.createMessage("0 args")
                    true
                },
                ArgumentCombination(listOf(StringArgument("name")), "tell me your name, and I will say Hello!") { event, arguments ->
                    Util.sendMessageInSameChannel(event, "Hello ${arguments["name"]}")
                    true
                },
                ArgumentCombination(listOf(literal("jay")), "ne net du jay", true) { event, _ ->
                    Util.sendMessageInSameChannel(event, "Verpiss dich, Jay!")
                    true
                },
                ArgumentCombination(listOf(literal("set"), string("name")), "berispel") { event, arguments ->
                    Util.sendMessageInSameChannel(event, "You have set ${arguments["name"]}")
                    true
                },
        )
}

object TagCommand : MessageCommand()
{
    override val ids: List<String>
        get() = listOf("tag", "t")
    override val needsNSFW: Boolean
        get() = false
    override val helpText: String
        get() = "create/get saved tags"
    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(listOf(literal("create"), string("tagName"), string("content")), "creates a new tag") { event, arguments ->
                    true
                },
                ArgumentCombination(listOf(string("tagName")), "displays a tag") { event, arguments ->
                    true
                },

                )

}

object NSFWCommand : MessageCommand()
{
    override val ids: List<String>
        get() = listOf("nsfw")
    override val needsNSFW: Boolean
        get() = true
    override val helpText: String
        get() = "nsfw"

    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf()
}

object AdminCommand : MessageCommand()
{
    override val ids: List<String>
        get() = listOf("admon")
    override val needsNSFW: Boolean
        get() = false
    override val helpText: String
        get() = "admon"

    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(listOf(), "nur für admons", true) { event, arguments -> true }
        )
}
