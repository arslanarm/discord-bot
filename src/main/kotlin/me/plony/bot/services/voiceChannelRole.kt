package me.plony.bot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.flow.collect
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
        launch {
            guild.members.collect {
                if (roleSnowflake in it.roleIds && it.getVoiceStateOrNull()?.channelId == null) {
                    it.removeRole(roleSnowflake)
                }
            }
        }
        guild.voiceStates.collect {
            val member = it.getMember()
            if (it.channelId != null && roleSnowflake !in member.roleIds) {
                member.addRole(roleSnowflake)
            }
        }
    }

    on<VoiceStateUpdateEvent> {
        if (state.guildId.asString != config.guild) return@on
        if (state.channelId != null) {
            val member = state.getMember()
            if (roleSnowflake !in member.roleIds) {
                member.addRole(roleSnowflake)
            }
        } else {
            val member = state.getMember()
            if (roleSnowflake in member.roleIds) {
                member.removeRole(roleSnowflake)
            }
        }

    }
}