package me.schlaubi.lavakord

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.VoiceChannel
import lavalink.client.io.Lavalink
import lavalink.client.io.Link
import me.schlaubi.lavakord.audio.KordLink

/**
 * Creates or returns an existing [Link] for the guild with the specified [guildId].
 *
 * @see Lavalink.getLink
 */
public fun <T : Link> Lavalink<T>.getLink(guildId: Snowflake): T = getLink(guildId.asString)

/**
 * Creates or returns an existing [Link] for this [Guild] using the [lavalink] instance.
 */
public fun GuildBehavior.getLink(lavalink: Lavalink<out Link>): Link = lavalink.getLink(id)

/**
 * Connects this link to the [voiceChannel].
 */
public suspend fun Link.connect(voiceChannel: VoiceChannel): Unit = connect(voiceChannel.id)

/**
 * Connects this link to the voice channel with the specified [snowflake].
 */
public suspend fun Link.connect(snowflake: Snowflake): Unit = connect(snowflake.value)

/**
 * Connects this link to the voice channel with the specified [voiceChannelId].
 */
public suspend fun Link.connect(voiceChannelId: String): Unit = connect(voiceChannelId.toLong())

/**
 * Connects this link to the voice channel with the specified [voiceChannelId]
 *
 * @throws me.schlaubi.lavakord.InsufficientPermissionException if the bot does not have the permission to join the voice channel or override user limit if needed
 * @throws NullPointerException If the [voiceChannelId] does not resolve to a valid voice channel
 */
public suspend fun Link.connect(voiceChannelId: Long): Unit =
    asKordLink().connect(voiceChannelId, true)

@PublishedApi
internal fun Link.asKordLink(): KordLink = (this as? KordLink) ?: error("This cannot be used on non Kord links")
