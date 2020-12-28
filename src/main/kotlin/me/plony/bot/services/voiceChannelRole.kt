package me.plony.bot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on


@Module
fun DiscordReceiver.voiceChannelRole() {
    @Serializable
    data class Config(val guild: String, val roleId: String)
    val config = readConfig(Config.serializer(), "data/voiceChanelRole", "config.json")
    val roleSnowflake = Snowflake(config.roleId)

    on<ReadyEvent> {
        println("here")
        val guild = kord.getGuild(Snowflake(config.guild))!!
        guild.members
            .filter { roleSnowflake in it.roleIds }
            .onEach { println(it) }
            .map { async { it.removeRole(roleSnowflake) } }
            .collect { it.await() }

        guild.channels
            .filterIsInstance<VoiceChannel>()
            .flatMapConcat { it.data.recipients.value?.asFlow() ?: emptyFlow() }
            .map { guild.getMember(it) }
            .filter { roleSnowflake !in it.roleIds }
            .collect { it.addRole(roleSnowflake) }
    }

    on<VoiceStateUpdateEvent> {
        if (state.guildId.asString != config.guild) return@on
        val member = state.getMember()
        println("${state.channelId} ${roleSnowflake in member.roleIds}")
        when {
            state.channelId != null && roleSnowflake !in member.roleIds ->
                member.addRole(roleSnowflake)
            state.channelId == null && roleSnowflake in member.roleIds ->
                member.removeRole(roleSnowflake)
        }
    }
}