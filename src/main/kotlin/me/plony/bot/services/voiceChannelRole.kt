package me.plony.bot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
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
        val guild = kord.getGuild(Snowflake(config.guild))!!

        guild.members
            .onEach {
                if (roleSnowflake in it.roleIds && it.getVoiceStateOrNull()?.channelId == null) {
                    it.removeRole(roleSnowflake)
                }
            }
            .launchIn(this)

        guild.voiceStates
            .onEach {
                val member = it.getMember()
                if (it.channelId != null && roleSnowflake !in member.roleIds) {
                    member.addRole(roleSnowflake)
                }
            }
            .launchIn(this)
    }

    on<VoiceStateUpdateEvent> {
        if (state.guildId.asString != config.guild) return@on

        val member = state.getMember()
        when {
            state.channelId != null && roleSnowflake !in member.roleIds ->
                member.addRole(roleSnowflake)
            state.channelId == null && roleSnowflake in member.roleIds ->
                member.removeRole(roleSnowflake)
        }
    }
}