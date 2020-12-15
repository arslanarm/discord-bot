package me.plony.bot.services

import com.gitlab.kordlib.core.behavior.channel.TextChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.VoiceChannel
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.kordLogger
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.io.Link
import me.plony.bot.music.GuildMusicManagers
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import me.schlaubi.lavakord.connect
import me.schlaubi.lavakord.lavalink
import me.schlaubi.lavakord.rest.TrackResponse.LoadType.*
import me.schlaubi.lavakord.rest.loadItem
import java.net.URI
import kotlin.time.milliseconds
import kotlin.time.seconds

@Module
fun DiscordReceiver.music() {
    val prefix = "!"

    // Creating Lavalink instance
    val lavalink = kord.lavalink {
        autoReconnect = true
    }
    // Adding Lavalink Node
    val password = System.getenv("LAVALINK_PASSWORD") ?: "secretpassword"
    lavalink.addNode(URI("ws://localhost:900"), password)

    // Shortcut for getting guild's link
    suspend fun MessageCreateEvent.link(): Link? {
        val guildId = guildId ?: return message.respond("Сервер не найден").let { null }
        return lavalink.getLink(guildId.value)
    }

    on<ReadyEvent> {
        lavalink.links.forEach {
            if (it.channel != null)
                it.disconnect()
        }
    }

    on<MessageCreateEvent> {
        // Filtering messages that starts with prefix
        val content = message.content
            .trim()
            .toLowerCase()
            .run {
                if (!startsWith(prefix)) return@on
                removePrefix(prefix).trim()
            }
        // Getting link
        val link = link() ?: return@on
        // Getting MusicManager
        val musicManager = GuildMusicManagers.createOrGet(link, this@music)

        // Function for getting author's voice channel and checking is he in the same channel as bot
        suspend fun getVoiceChannelAndCheck(): VoiceChannel? {
            // Bot is not in the voice channel
            if (link.channel == null) return message
                .respond("Я не в войсе").run { null }
            // Author is not in the voice channel
            val voiceChannel = message.authorsVoiceState()?.getChannel() ?: return message
                .respond("Вы должны быть в голосовом канале").run { null }
            // Bot and Author are in the different channels
            if (link.channel != voiceChannel.id.value) return message
                .respond("Вы должны быть в том же голосовом что и я").run { null }
            return voiceChannel
        }

        when {
            content.startsWith("сыграй") -> {
                // Getting voice channel and checking is the member in the voice or not
                val voiceChannel = message.authorsVoiceState()?.channelId ?: return@on message
                    .respond("Вы должны быть в голосовом канале")

                // Checking is the member in the same voice
                if (link.channel != null
                    && link.channel != voiceChannel.value) return@on message
                    .respond("Вы должны быть в том же голосовом что и я")

                val query = content.removePrefix("сыграй").trim().run {
                    if (startsWith("http")) this
                    else "ytsearch: $this"
                }
                // Loading the track
                val result = link.loadItem(query)
                try {
                    when (result.loadType) {
                        // If it loaded adding it to the queue
                        TRACK_LOADED, SEARCH_RESULT -> {
                            val track = result.tracks.first().toAudioTrack()
                            message.channel.createEmbed {
                                description = """
                                    Добавляю в очередь ${track.stringWithUrl()}
                                """.trimIndent()
                            }
                            musicManager.add(track, message.channel)
                        }
                        PLAYLIST_LOADED -> {
                            val selectedTrack = result.playlistInfo.selectedTrack!!
                            val tracks = result.tracks
                                .drop(selectedTrack)
                                .map { it.toAudioTrack() }
                            message.channel.createEmbed {
                                description = """
                                    Загружен плейлист [${result.playlistInfo.name}](${result.tracks.first().info.uri})
                                    Размер плейлиста: ${result.tracks.size} 
                                    Список песень: ${tracks.joinToString("\n\t") { it.stringWithUrl() }}
                                    """.trimIndent()
                            }
                            tracks.forEach { musicManager.add(it, message.channel)}
                        }
                        // In other cases throwing the exception
                        else -> throw LoadException()
                    }
                    // Connecting to the channel if you are not in it
                    if (link.channel == null)
                        link.connect(voiceChannel)
                } catch (e: LoadException) {
                    // Handling the cases when track is not loaded properly
                    message.respond("Произошла ошибка при загрузке трека")
                    kordLogger.error(result.exception.toString())
                }
            }
            content == "зайди" -> {
                // We are already in the voice channel
                if (link.channel != null) return@on message
                    .respond("Я уже в войсе")
                // Author is not in a voice channel
                val voiceChannel = message.authorsVoiceState()?.channelId ?: return@on message
                    .respond("Вы должны быть в голосовом канале")

                link.connect(voiceChannel)
            }
            content == "выйди" -> {
                getVoiceChannelAndCheck()
                musicManager.stop()
                link.disconnect()
            }
            content == "стоп" -> {
                getVoiceChannelAndCheck()
                musicManager.stop()
            }
            content.startsWith("скип") -> {
                getVoiceChannelAndCheck()
                // Getting the amount to skip. Default 1
                val amount = content.removePrefix("скип")
                    .trim()
                    .run {
                        if (isBlank()) 1
                        else toIntOrNull()
                    } ?: return@on message.respond("Количество для скипа должно быть числом")
                musicManager.next(amount)
            }
            content == "очередь" -> {
                if (link.player.playingTrack == null) {
                    return@on message.respond("Сейчас ничего не играет")
                }
                message.channel.createEmbed {
                    description = """
                        Очередь: ${link.player.playingTrack?.stringWithUrl()}
                            ${musicManager.queue.withIndex().joinToString("\n\t") { (index, it) -> "${index + 1}) ${it.stringWithUrl()}" }}
                    """.trimIndent()
                }
            }
        }
    }
}

class LoadException : Exception()

fun AudioTrack.stringWithUrl() = "[${string()}](${info.uri})"

fun AudioTrack.string() = with(info){
    "$author - $title: ${
        duration.milliseconds.toComponents { hours, minutes, seconds, _ -> 
            "Длительность ${if (hours > 0) "$hours:" else ""}${minutes.withZero()}:${seconds.withZero()}" 
        }
    }"
}
fun Int.withZero() = if (this > 9) "$this" else "0$this"
suspend fun Message.authorsVoiceState() = getAuthorAsMember()?.getVoiceStateOrNull()
suspend fun Message.respond(message: String) = channel.createMessage(message).let{}