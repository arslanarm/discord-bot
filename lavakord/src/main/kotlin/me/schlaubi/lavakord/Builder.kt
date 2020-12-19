package me.schlaubi.lavakord


import dev.kord.core.Kord
import lavalink.client.io.Lavalink
import lavalink.client.io.Link
import me.schlaubi.lavakord.audio.KordLavaLink

/**
 * Creates a [Lavalink] instance for this [Kord] instance.
 *
 * @param configure a receiver configuring the [KordLinkOptions] instance used for configuration of this instance
 */
@Suppress("unused")
public fun Kord.lavalink(configure: MutableKordLinkOptions.() -> Unit = {}): Lavalink<out Link> {
    val options = MutableKordLinkOptions().apply(configure).seal()
    return KordLavaLink(
        this,
        options,
        selfId.asString,
        resources.shardCount
    )
}

/**
 * Interface representing options for Kordlink.
 *
 * @property autoReconnect Whether to auto-reconnect links or not
 */
public interface KordLinkOptions {
    public val autoReconnect: Boolean
}

/**
 * Mutable implementation of [KordLinkOptions].
 */
public data class MutableKordLinkOptions(override var autoReconnect: Boolean = true) : KordLinkOptions {
    internal fun seal() = ImmutableKordLinkOptions(autoReconnect)
}

internal data class ImmutableKordLinkOptions(override val autoReconnect: Boolean) : KordLinkOptions
