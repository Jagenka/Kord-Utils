package de.jagenka

import de.jagenka.Argument.Companion.literal
import de.jagenka.Argument.Companion.string
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
            registry.register(HelpMessageCommand(registry))

//            registry.register(NSFWCommand)
//            registry.register(AdminCommand)

            registry.needsNSFWResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.x)
            }

            registry.needsAdminResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.facePalm)
            }

            // scheinbar passiert nach login nichts mehr
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
    override val needsAdmin: Boolean
        get() = false
    override val needsNSFW: Boolean
        get() = false

    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(emptyList(), "aus nichts kommt nichts") { event, _ ->
                    event.message.channel.createMessage("0 args")
                    true
                },
                ArgumentCombination(listOf(string("name")), "tell me your name, and I will say Hello!") { event, arguments ->
                    Util.sendMessageInSameChannel(event, "Hello ${arguments["name"]}")
                    true
                },
                ArgumentCombination(listOf(literal("jay")), "ne net du jay") { event, _ ->
                    Util.sendMessageInSameChannel(event, "Verpiss dich, Jay!")
                    true
                },
        )
}

/*object NSFWCommand : MessageCommand()
{
    override val prefix: String
        get() = "!"
    override val names: List<String>
        get() = listOf("nsfw", "filth", "nixschlimmes")
    override val shortHelpText: String
        get() = "short help"
    override val longHelpText: String
        get() = "long help"
    override val needsAdmin: Boolean
        get() = false
    override val needsNSFW: Boolean
        get() = true

    override fun prepare(kord: Kord)
    {
        println("NSFWCommand ready.")
    }

    override suspend fun execute(event: MessageCreateEvent, args: List<String>)
    {
        Util.sendMessageInSameChannel(event, "kinky")
    }
}

object AdminCommand : MessageCommand()
{
    override val prefix: String
        get() = "!"
    override val names: List<String>
        get() = listOf("admin")
    override val shortHelpText: String
        get() = "short help"
    override val longHelpText: String
        get() = "long help"
    override val needsAdmin: Boolean
        get() = true
    override val needsNSFW: Boolean
        get() = false

    override fun prepare(kord: Kord)
    {
        println("AdminCommand ready.")
    }

    override suspend fun execute(event: MessageCreateEvent, args: List<String>)
    {
        Util.sendMessageInSameChannel(event, Emojis.`+1`.code)
    }
}*/
