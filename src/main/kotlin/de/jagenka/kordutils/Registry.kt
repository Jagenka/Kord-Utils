package de.jagenka.kordutils

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

class Registry(
    val kord: Kord,
    /**
     * What a message must start with, so that it is considered by this registry.
     */
    val messageCommandPrefix: String,
    val interactsWithBots: Boolean = true,
    val commandLocksBehaviors: Boolean = true
)
{
    internal val commands = mutableMapOf<String, MessageCommand>()
    internal val behaviors = mutableSetOf<Behavior>()

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

            if (!message.content.startsWith(messageCommandPrefix)) return@on

            val args = this.message.content.split(" ")
            val firstWord = args.getOrNull(0) ?: return@on

            val command = commands[firstWord.removePrefix(messageCommandPrefix)] ?: return@on

            if (command.needsNSFW && !this.message.channel.fetchChannel().data.nsfw.discordBoolean) // channel is not NSFW, but needs to be
            {
                needsNSFWResponse.invoke(this)
                return@on
            }

            if (!commandLocksBehaviors || command.run(this, args))
            {
                behaviors.forEach { behavior ->
                    this.guildId?.let { guildSF ->
                        if (behavior.isEnabled(guildSF.value, this.message.channelId.value)) behavior.run(this)
                    }
                }
            }
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
        command.prepare(this)
    }

    internal suspend fun getShortHelpTexts(event: MessageCreateEvent): List<String>
    {
        return commands.values.toSortedSet().filter { isSenderAdmin.invoke(event) || it.allowedArgumentCombinations.any { !it.needsAdmin } }
            .map { it.ids.joinToString(separator = "|", postfix = ": ${it.helpText}") { "`$messageCommandPrefix$it`" } }
    }

    internal suspend fun getHelpTextsForCommand(id: String, event: MessageCreateEvent): List<String>
    {
        return commands[id]?.allowedArgumentCombinations?.filter { !it.needsAdmin || isSenderAdmin.invoke(event) }?.map {
            it.arguments.joinToString(prefix = "`$messageCommandPrefix$id ", separator = " ") {
                it.displayInHelp
            }.trim() + "`: ${it.helpText}"
        } ?: listOf("`$messageCommandPrefix$id` is not a valid command.")
    }
}