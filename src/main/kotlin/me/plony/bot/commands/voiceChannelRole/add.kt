package me.plony.bot.commands.voiceChannelRole

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.live.live
import dev.kord.core.on
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.plony.bot.extensions.VoiceChannelRoleExtension
import me.plony.bot.utils.config
import me.plony.bot.utils.kord
import me.plony.bot.utils.member

suspend fun VoiceChannelRoleExtension.add() = with(kord) {
    @Serializable
    data class Config(val guild: String, val roleId: String)
    val config = config(Config.serializer(), "voiceChanelRole")
    val roleSnowflake = Snowflake(config.roleId)
    on<VoiceStateUpdateEvent> {
        if (state.guildId != Snowflake(config.guild)) return@on
        val left = old?.channelId
        val joined = state.channelId
        val member = state.member
        println("MEMBER: ${member.nicknameMention} LEFT: ${left?.asString} JOINED ${joined?.asString}")
        when {
            left == null && joined != null ->
                member.addRole(roleSnowflake)
            left != null && joined == null ->
                member.removeRole(roleSnowflake)
        }
    }
}