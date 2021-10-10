package me.plony.bot.commands

import dev.kord.common.entity.*
import dev.kord.core.behavior.channel.VoiceChannelBehavior
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import me.jakejmattson.discordkt.api.dsl.listeners
import me.plony.bot.config.Config
import me.plony.bot.config.PrivateChannelConfig
import kotlin.time.Duration

sealed class PrivateChannelEvent

object JoinedEvent : PrivateChannelEvent()
object LeftEvent : PrivateChannelEvent()

class PrivateChannel(
    val channel: VoiceChannelBehavior
) {
    val users = mutableListOf<Member>()
    val events = MutableSharedFlow<PrivateChannelEvent>()

    suspend fun join(user: Member) {
        if (user in users) {
            kordLogger.info(
                "Something wrong happened. " +
                        "User ${user.nickname} joined into ${channel.asChannel().name}." +
                        "But here are the users in the channel: $users"
            )
            return
        }
        users.add(user)
        events.emit(JoinedEvent)
    }

    suspend fun leave(user: Member): Boolean {
        if (user !in users) {
            kordLogger.info(
                "Something wrong happened. " +
                        "User ${user.nickname} left from ${channel.asChannel().name}." +
                        "But here are the users in the channel: $users"
            )
            return false
        }
        users.remove(user)
        events.emit(LeftEvent)
        return users.isEmpty().also { if (it) channel.delete() }
    }
}

class PrivateChannelManager {
    private val privateChannels = mutableMapOf<Snowflake, PrivateChannel>()

    suspend fun leave(channelId: Snowflake, user: Member) {
        val destroyed = privateChannels[channelId]?.leave(user) ?: false
        if (destroyed) privateChannels.remove(channelId)
    }

    suspend fun join(channelId: Snowflake, user: Member) {
        privateChannels[channelId]?.join(user)
    }

    suspend fun create(parent: Snowflake, guild: Guild, user: Member) = coroutineScope {
        val voice = createVoiceChannel(guild, user, parent)
        val privateChannel = PrivateChannel(voice)
        privateChannels[voice.id] = privateChannel
        launch {
            val joined = checkJoined(privateChannel, Duration.seconds(10))
            if (!joined) {
                voice.delete()
                privateChannels.remove(voice.id)
            }
        }
        user.edit {
            voiceChannelId = voice.id
        }
    }

    private suspend fun checkJoined(privateChannel: PrivateChannel, duration: Duration): Boolean {
        val joined = withTimeoutOrNull(duration) {
            privateChannel.events
                .filterIsInstance<JoinedEvent>()
                .first()
        }
        return joined != null
    }

    private suspend fun createVoiceChannel(
        guild: Guild,
        user: Member,
        parent: Snowflake
    ): VoiceChannel {
        val voice = guild.createVoiceChannel("Канал ${user.displayName}") {
            parentId = parent
            val overwrite = Overwrite(
                user.id,
                OverwriteType.Member,
                Permissions(Permission.ManageChannels),
                Permissions()
            )
            permissionOverwrites.add(overwrite)
        }
        return voice
    }

}

fun privateChannels() = listeners {
    Config.privateChannels.forEach { config ->
        val manager = PrivateChannelManager()
        on<VoiceStateUpdateEvent> {
            val oldState = old
            if (oldState?.channelId != null) {
                manager.leave(oldState.channelId!!, state.getMember())
            }
            val newState = state
            if (newState.channelId != null) {
                val member = state.getMember()
                manager.join(state.channelId!!, member)
                if (newState.channelId?.value == config.createChannel) {
                    manager.create(Snowflake(config.createCategory), state.getGuild(), member)
                }
            }
        }
    }
}