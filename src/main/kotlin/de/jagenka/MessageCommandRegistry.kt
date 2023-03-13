package de.jagenka

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

/**
 * Registry for all MessageCommands belonging to a Kord instance. The bot needs to have message read access, otherwise this will not work.
 * Make sure to create an instance of this before you login your bot with Kord::login
 */

class MessageCommandRegistry(
        val kord: Kord,
        /**
         * What a message must start with, so that it is considered by this registry.
         */
        val prefix: String,
        val interactsWithBots: Boolean = true
)
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

    var isSenderAdmin: suspend (event: MessageCreateEvent) -> Boolean = {
        it.member?.isOwner() == true // TODO: adminRole
    }

    init
    {
        kord.on<MessageCreateEvent> {
            if (interactsWithBots)
            {
                // return if author is self or undefined
                if (message.author?.id == kord.selfId) return@on
            } else
            {
                // return if author is a bot or undefined
                if (message.author?.isBot != false) return@on
            }

            if (!message.content.startsWith(prefix)) return@on

            val args = this.message.content.split(" ")
            val firstWord = args.getOrNull(0) ?: return@on

            val command = commands[firstWord.removePrefix(prefix)] ?: return@on

            if (command.needsNSFW && !this.message.channel.fetchChannel().data.nsfw.discordBoolean) // channel is not NSFW, but needs to be
            {
                needsNSFWResponse.invoke(this)
                return@on
            }

            command.run(this, args)
        }
    }

    /**
     * Register your own implementation of a MessageCommand here. This method will also call `command.prepare(kord)`.
     * @param command is said custom implementation of MessageCommand.
     */
    fun register(command: MessageCommand)
    {
        command.ids.forEach {
            if (commands.containsKey(it)) error("command id `$it` is already assigned to command ${commands[it]}")
            commands[it] = command
        }
        command.registry = this
        command.prepare(kord)
    }

    internal suspend fun getShortHelpTexts(event: MessageCreateEvent): List<String>
    {
        return commands.values.toSortedSet().filter { isSenderAdmin.invoke(event) || it.allowedArgumentCombinations.any { !it.needsAdmin } }.map { it.ids.joinToString(separator = "|", postfix = ": ${it.helpText}") { "`$prefix$it`" } }
    }

    internal suspend fun getHelpTextsForCommand(id: String, event: MessageCreateEvent): List<String>
    {
        return commands[id]?.allowedArgumentCombinations?.filter { !it.needsAdmin || isSenderAdmin.invoke(event) }?.map {
            it.arguments.joinToString(prefix = "`$prefix$id ", separator = " ") {
                it.displayInHelp
            }.trim() + "`: ${it.helpText}"
        } ?: listOf("`$prefix$id` is not a valid command.")
    }
}