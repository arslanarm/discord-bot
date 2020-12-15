package me.plony.bot.music

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.TextChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.CoroutineScope
import lavalink.client.io.Link
import lavalink.client.player.event.TrackEndEvent
import lavalink.client.player.event.TrackStartEvent
import me.plony.bot.services.string
import me.schlaubi.lavakord.audio.on

class MusicManager(private val link: Link, coroutineScope: CoroutineScope) {
    val queue = object : ArrayList<AudioTrack>() {
        override fun remove(element: AudioTrack): Boolean {
            val result = super.remove(element)
            if (isEmpty())
                textChannel = null
            return result
        }
    }
    private var textChannel: MessageChannelBehavior? = null
    init {
        link.player.on<TrackStartEvent>(coroutineScope) {
            textChannel?.createEmbed {
                description = """
                    Играет: [${track.string()}](${track.info.uri})
                """.trimIndent()
            }
        }
        link.player.on<TrackEndEvent>(coroutineScope) {
            next()
        }
    }

    fun next(amount: Int = 1): Boolean = with(link.player) {
        if (queue.isEmpty()) return@with false
        var nextTrack = queue.removeLast()
        repeat(amount - 1) { nextTrack = queue.removeLast() }
        playTrack(nextTrack)
        true
    }

    fun stop() {
        queue.clear()
        if (link.player.playingTrack != null)
            link.player.stopTrack()
    }

    fun add(track: AudioTrack, channel: MessageChannelBehavior) {
        if (textChannel == null)
            textChannel = channel

        queue.add(track)
        if (link.player.playingTrack == null)
            next()
    }
}