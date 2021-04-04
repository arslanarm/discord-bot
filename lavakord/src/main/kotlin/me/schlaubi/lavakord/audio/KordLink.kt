package me.schlaubi.lavakord.audio

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.OptionalLong
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.gateway.Gateway
import dev.kord.gateway.UpdateVoiceStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import lavalink.client.io.Link
import lavalink.client.player.LavalinkPlayer
import lavalink.client.player.event.IPlayerEventListener
import lavalink.client.player.event.PlayerEvent
import me.schlaubi.lavakord.InsufficientPermissionException

@PublishedApi
internal val KordLink.client: Kord
    get() = lavalink.client

@OptIn(ExperimentalCoroutinesApi::class)
internal class KordLink(internal val lavalink: KordLavaLink, guildId: String?) : Link(lavalink, guildId),
    IPlayerEventListener {


    private var _player: LavalinkPlayer? = null

    private var eventPublisher: BroadcastChannel<PlayerEvent> = BroadcastChannel(1)

    @OptIn(FlowPreview::class)
    internal val events: Flow<PlayerEvent>
        get() = eventPublisher.asFlow().buffer(Channel.UNLIMITED)

    override fun getPlayer(): LavalinkPlayer {
        if (_player == null) {
            _player = super.getPlayer()
            _player!!.addListener(this)
        }
        return _player!!
    }

    override fun resetPlayer() {
        super.resetPlayer()
        _player = null
        eventPublisher.close()
    }

    public override fun removeConnection() {
        // JDA handles this for us without even being in class path
    }

    override fun queueAudioDisconnect() {
        lavalink.client.launch {
            val guild = lavalink.client.getGuild(Snowflake(guildIdLong)) ?: error("Could not find Guild $guildId")

            guild.localGateway.send(
                UpdateVoiceStatus(
                    Snowflake(guildIdLong),
                    null,
                    selfMute = false,
                    selfDeaf = false
                )
            )
        }
    }

    override fun queueAudioConnect(channelId: Long) {
        lavalink.client.launch {
            val channel = lavalink.client.getChannel(Snowflake(channelId)) as VoiceChannel
            channel.getGuild().localGateway.send(
                UpdateVoiceStatus(
                    Snowflake(guildIdLong),
                    channel.id,
                    selfMute = false,
                    selfDeaf = false
                )
            )
        }
    }

    suspend fun connect(channelId: Long, checkChannel: Boolean = true) {
        val channel = lavalink.client.getChannel(Snowflake(channelId)) as? VoiceChannel
        requireNotNull(channel) { "channelId must be the valid id of a voice channel" }
        require(channel.guildId.asString == guildId) {
            "The provided VoiceChannel is not a part of the Guild that this AudioManager handles. Please provide a VoiceChannel from the proper Guild"
        }
        val permissions = channel.getEffectivePermissions(lavalink.client.selfId)
        if (!permissions.contains(Permission.Connect) && !permissions.contains(Permission.MoveMembers)) {
            throw InsufficientPermissionException(Permission.Connect)
        }

        val voiceState = channel.getGuild().getMember(lavalink.client.selfId).getVoiceStateOrNull()
        if (checkChannel && channel.id == voiceState?.channelId) return

        if (voiceState?.channelId != null) {
            val limit = channel.userLimit
            if (!permissions.contains(Permission.Administrator)) {
                if (limit > 0
                    && limit >= channel.getGuild().voiceStates.count { it.channelId == channel.id }
                    && !permissions.contains(Permission.MoveMembers)
                )
                    throw InsufficientPermissionException(Permission.MoveMembers)
            }
        }

        state = State.CONNECTING
        queueAudioConnect(channelId)
    }

    override fun onEvent(event: PlayerEvent) {
        lavalink.client.launch { eventPublisher.send(event) }
    }
}

private val Guild.localGateway: Gateway
    get() = kord.gateway.gateways[gatewayId] ?: error("Could not find guild gateway")

// https://discord.com/developers/docs/topics/gateway#sharding-sharding-formula
private val Guild.gatewayId: Int
    get() = ((id.value shl 22) % kord.resources.shards.totalShards).toInt()
