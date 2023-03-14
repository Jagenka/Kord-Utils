package de.jagenka.kordutils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object Config
{
    private val serializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val configs: MutableMap<ULong, ConfigStructure> = mutableMapOf()
    private val pathToConfigDir = Path("kord-utils/config/")

    init
    {
        if (Files.notExists(pathToConfigDir))
        {
            Files.createDirectory(pathToConfigDir)
        }

        pathToConfigDir.listDirectoryEntries().forEach { path ->
            configs[path.name.removeSuffix(".json").toULong()] = serializer.decodeFromString(path.toFile().readText())
        }
    }

    internal fun setAdminRoleId(guild: ULong, adminRoleId: ULong)
    {
        getConfigForGuild(guild).adminRoleId = adminRoleId
        storeConfigForGuild(guild)
    }

    internal fun getAdminRoleId(guild: ULong): ULong?
    {
        return getConfigForGuild(guild).adminRoleId
    }

    internal fun getConfigForGuild(id: ULong): ConfigStructure
    {
        return configs.getOrPut(id) { ConfigStructure() }
    }

    internal fun storeConfigForGuild(id: ULong)
    {
        val pathToGuildConfig = pathToConfigDir.resolve("$id.json")
        if (Files.notExists(pathToGuildConfig))
        {
            Files.createFile(pathToGuildConfig)
        }

        Files.writeString(pathToGuildConfig, serializer.encodeToString(getConfigForGuild(id)))
    }

    //region Behaviors

    internal fun enableBehavior(guild: ULong, channel: ULong, behavior: String)
    {
        getConfigForGuild(guild).behaviorSettings.getOrPut(channel) { mutableSetOf() }.add(behavior)
        storeConfigForGuild(guild)
    }

    internal fun enableAllBehaviors(guild: ULong, channel: ULong, behaviors: List<Behavior>)
    {
        getConfigForGuild(guild).behaviorSettings[channel] = behaviors.map { behavior -> behavior::class.simpleName.toString() }.toMutableSet()
        storeConfigForGuild(guild)
    }

    internal fun disableBehavior(guild: ULong, channel: ULong, behavior: String)
    {
        getConfigForGuild(guild).behaviorSettings[channel]?.remove(behavior)
        storeConfigForGuild(guild)
    }

    internal fun disableAllBehaviors(guild: ULong, channel: ULong)
    {
        getConfigForGuild(guild).behaviorSettings[channel] = mutableSetOf()
        storeConfigForGuild(guild)
    }

    internal fun isBehaviorEnabled(guild: ULong, channel: ULong, behavior: String): Boolean
    {
        return getConfigForGuild(guild).behaviorSettings[channel]?.contains(behavior) ?: return false
    }

    internal fun getEnabledBehaviors(guild: ULong, channel: ULong): Set<String>
    {
        return getConfigForGuild(guild).behaviorSettings[channel]?.toSet() ?: emptySet()
    }

    //endregion
}

@Serializable
data class ConfigStructure(
    var adminRoleId: ULong? = null,
    var behaviorSettings: MutableMap<ULong, MutableSet<String>> = mutableMapOf(), // key is Channel Snowflake, value is list of enabled behaviors
)