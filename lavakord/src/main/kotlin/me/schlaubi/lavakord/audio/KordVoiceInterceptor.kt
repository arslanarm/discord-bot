package me.schlaubi.lavakord.audio


import dev.kord.core.event.guild.VoiceServerUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import lavalink.client.io.Link
import org.json.JSONObject

internal class KordVoiceInterceptor(private val lavalink: KordLavaLink) {

    init {
        lavalink.client.on(consumer = ::handleVoiceServerUpdate)
        lavalink.client.on(consumer = ::handleVoiceStateUpdate)
    }

    private suspend fun handleVoiceServerUpdate(event: VoiceServerUpdateEvent) {
        val guild = event.getGuild()

        val json = JSONObject()
            .put("token", event.token)
            .put("guild_id", guild.id.value)
            .put("endpoint", event.endpoint)

        lavalink.getLink(guild.id.asString).onVoiceServerUpdate(
            json,
            guild.getMember(lavalink.client.selfId).getVoiceState().sessionId
        )
    }

    private suspend fun handleVoiceStateUpdate(event: VoiceStateUpdateEvent) {
        val channel = event.state.getChannelOrNull()
        val link = event.state.guildId.let { lavalink.getLink(it.asString) }

        // Null channel means disconnected
        if (channel == null) {
            if (link.state != Link.State.DESTROYED) {
                link.onDisconnected()
            }
        } else {
            link.setChannel(channel.id.asString)
        }
    }
}
