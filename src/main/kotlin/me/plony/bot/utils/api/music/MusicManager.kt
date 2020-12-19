package me.plony.bot.utils.api.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lavalink.client.io.Link
import lavalink.client.player.event.TrackEndEvent
import lavalink.client.player.event.TrackStartEvent
import me.plony.bot.utils.shortcuts.string
import me.schlaubi.lavakord.audio.on
import kotlin.time.minutes

class MusicManager(private val link: Link, coroutineScope: CoroutineScope) {
    companion object {
        val TIME_TO_DISCONNECT = 3.minutes
    }
    internal var quittingJob: Job? = null
    val queue = arrayListOf<AudioTrack>()
    private var textChannel: MessageChannelBehavior? = null
    init {
        link.player.on<TrackStartEvent>(coroutineScope) {
            textChannel?.createEmbed {
                description = """
                    Играет: [${track.string()}](${track.info.uri})
                """.trimIndent()
            }
            if (quittingJob != null) {
                quittingJob?.cancel()
                quittingJob = null
            }
        }
        link.player.on<TrackEndEvent>(coroutineScope) {
            next()
            if (queue.isEmpty() && player.playingTrack == null) {
                textChannel = null
                if (quittingJob == null)
                    quittingJob = coroutineScope.launch {
                        delay(TIME_TO_DISCONNECT)
                        link.disconnect()
                    }
            }
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