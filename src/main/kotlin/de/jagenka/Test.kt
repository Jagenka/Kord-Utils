package de.jagenka

import dev.kord.common.entity.Snowflake
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
        val adminRoleSnowflake = Snowflake(System.getenv()["ADMIN_ROLE_ID"]?.toULong() ?: error("error reading admin role id"))

        runBlocking {
            val kord = Kord(token)

            val registry = MessageCommandRegistry(kord, adminRoleSnowflake)
            registry.register(NormalCommand)
//            registry.register(NSFWCommand)
//            registry.register(AdminCommand)
            registry.register(HelpMessageCommand(registry, "!"))

            registry.needsNSFWResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.x)
            }

            registry.needsAdminResponse = { event ->
                Util.addReactionToMessage(event.message, Emojis.facePalm)
            }

            // scheinbar passiert nach login nichts mehr
            kord.login {// TODO: move?
                @OptIn(PrivilegedIntent::class)
                intents += Intent.MessageContent
            }
        }
    }
}

object NormalCommand : MessageCommand()
{
    override val prefix: String
        get() = "!"
    override val names: List<String>
        get() = listOf("normal")
    override val shortHelpText: String
        get() = "short help"
    override val longHelpText: String
        get() = "long help"
    override val needsAdmin: Boolean
        get() = false
    override val needsNSFW: Boolean
        get() = false

    override fun prepare(kord: Kord)
    {
        println("NormalCommand ready.")
    }

    override val allowedArgumentCombinations: List<ArgumentCombination>
        get() = listOf(
                ArgumentCombination(emptyList()) { event, args ->
                    event.message.channel.createMessage("Dies ist was Sie tippten: $args")
                    true
                },
                ArgumentCombination(listOf(Argument("a", ArgumentType.STRING))) { event, args ->
                    Util.sendMessageInSameChannel(event, "domcommand > subcommand")
                    true
                }
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
