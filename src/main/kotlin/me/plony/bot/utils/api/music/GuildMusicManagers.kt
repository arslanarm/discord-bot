package me.plony.bot.utils.api.music

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.CoroutineScope
import lavalink.client.io.Link

object GuildMusicManagers {
    private val musicManagers = mutableMapOf<Snowflake, MusicManager>()
    operator fun get(snowflake: Snowflake) = musicManagers[snowflake]
    fun createOrGet(link: Link, coroutineScope: CoroutineScope): MusicManager {
        val snowflake = Snowflake(link.guildIdLong)
        musicManagers[snowflake].run { if (this != null) return this }

        val musicManager = MusicManager(link, coroutineScope)
        musicManagers[snowflake] = musicManager
        return musicManager
    }
}