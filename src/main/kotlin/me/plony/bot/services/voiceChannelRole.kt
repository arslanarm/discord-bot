package me.plony.bot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.requestMembers
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.live.live
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.plony.bot.utils.api.inline.asMember
import me.plony.bot.utils.api.inline.member
import me.plony.bot.utils.asyncMap
import me.plony.bot.utils.onEachLaunch
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
//        guild.withStrategy(EntitySupplyStrategy.rest).members
//            .asyncMap(this) { it.asMember() }
//            .onEach { println(it.displayName) }
//            .filter { roleSnowflake in it.roleIds && it.getVoiceStateOrNull()?.channelId == null }
//            .asyncMap(this) { it.removeRole(roleSnowflake) }
//            .launchIn(this@voiceChannelRole)
//            .invokeOnCompletion {
//                println("Members Completed")
//            }

        guild.channels
            .filterIsInstance<VoiceChannel>()
            .flatMapConcat { it.voiceStates }
            .asyncMap(this) { it.getMember() }
            .filter { roleSnowflake !in it.roleIds }
            .asyncMap(this) { it.addRole(roleSnowflake) }
            .launchIn(this@voiceChannelRole)
            .invokeOnCompletion {
                println("Members in Channel Completed")
            }

        guild.live()
            .events
            .filterIsInstance<VoiceStateUpdateEvent>()
            .onEachLaunch(this) {
                val left = it.old?.channelId
                val joined = it.state.channelId
                val member = it.state.member
                println("${member.id} joined: $joined left: $left")
                when {
                    left == null && joined != null ->
                        member.addRole(roleSnowflake)
                    left != null && joined == null ->
                        member.removeRole(roleSnowflake)
                }
            }
            .launchIn(this@voiceChannelRole)
    }
}