package me.schlaubi.lavakord.audio

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.channel.VoiceChannelDeleteEvent
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.on
import kotlinx.coroutines.launch
import lavalink.client.io.Lavalink
import me.schlaubi.lavakord.KordLinkOptions

internal class KordLavaLink(
    internal val client: Kord,
    private val options: KordLinkOptions,
    userId: String?,
    numShards: Int
) : Lavalink<KordLink>(userId, numShards) {

    init {
        KordVoiceInterceptor(this)

        client.on(consumer = ::onReconnect)
        client.on(consumer = ::onLeave)
        client.on(consumer = ::onChannelDeletion)
    }

    override fun buildNewLink(guildId: String?): KordLink = KordLink(this, guildId)

    private fun onReconnect(event: DisconnectEvent.ReconnectingEvent) {
        client.launch {
            if (options.autoReconnect) {
                linksMap.forEach { (guildId, link) ->
                    val lastChannel = link.lastChannel
                    if (lastChannel != null && event.kord.getGuild(Snowflake(guildId)) != null) {
                        try {
                            link.connect(lastChannel.toLong(), false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun onLeave(event: GuildDeleteEvent) {
        if (!event.unavailable) {
            linksMap[event.guildId.asString]?.removeConnection()
        }
    }

    private fun onChannelDeletion(event: VoiceChannelDeleteEvent) {
        val link = linksMap[event.channel.guildId.asString]
        if (event.channel.id.asString == link?.lastChannel) {
            link.removeConnection()
        }
    }
}
