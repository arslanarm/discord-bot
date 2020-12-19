package me.plony.bot.utils.shortcuts

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.time.milliseconds

fun AudioTrack.string() = with(info){
    "$author - $title: ${
        duration.milliseconds.toComponents { hours, minutes, seconds, _ ->
            "Длительность ${if (hours > 0) "$hours:" else ""}${minutes.withZero()}:${seconds.withZero()}"
        }
    }"
}

private fun Int.withZero() = if (this > 9) "$this" else "0$this"