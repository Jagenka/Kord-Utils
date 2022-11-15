package de.jagenka

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

/**
 * Registry for all MessageCommands belonging to a Kord instance. The bot needs to have message read access, otherwise this will not work.
 * Make sure to create an instance of this before you login your bot with Kord::login
 */
class MessageCommandRegistry(val kord: Kord, val adminRoleId: Snowflake)
{
    private val commands = mutableMapOf<String, MessageCommand>()

    /**
     * suspend function to be called if a command needs admin, but the sender does not have the admin role.
     */
    var needsAdminResponse: suspend (event: MessageCreateEvent) -> Unit = {
        it.message.channel.createMessage("You need to be admin to do that!")
    }

    /**
     * suspend function to be called if a command can only execute in a channel marked "NSFW", but isn't marked as such.
     */
    var needsNSFWResponse: suspend (event: MessageCreateEvent) -> Unit = {
        it.message.channel.createMessage("You can only do that in a channel marked \"NSFW\"!")
    }

    init
    {
        kord.on<MessageCreateEvent> {
            // return if author is a bot or undefined
            if (message.author?.isBot != false) return@on

            val args = this.message.content.split(" ")
            val firstWord = args.getOrNull(0) ?: return@on
            val command = commands[firstWord] ?: return@on

            if (command.needsAdmin && this.member?.roleIds?.contains(adminRoleId) != true) // command needs admin, but sender does not have the admin role
            {
                needsAdminResponse.invoke(this)
                return@on
            }

            if (command.needsNSFW && !this.message.channel.fetchChannel().data.nsfw.discordBoolean) // channel is not NSFW, but needs to be
            {
                needsNSFWResponse.invoke(this)
                return@on
            }

            command.findSubcommand(0, this, args)
        }
    }

    /**
     * Register your own implementation of a MessageCommand here. This method will also call `command.prepare(kord)`.
     * @param command is said custom implementation of MessageCommand.
     */
    fun register(command: MessageCommand)
    {
        command.firstWords.forEach { commands[it] = command }
        command.prepare(kord)
    }

    internal fun getShortHelpTexts(): List<String>
    {
        val result = mutableListOf<String>()
        commands.values.toSortedSet { one, two -> one.commandExample.compareTo(two.commandExample) }
                .forEach { cmd -> result.addAll(cmd.getPreorderWithPrefix("").map { "`${it.first}`: ${it.second.shortHelpText}" }) }
        return result
    }
}