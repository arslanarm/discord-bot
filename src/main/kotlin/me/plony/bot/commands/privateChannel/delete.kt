package me.plony.bot.commands.privateChannel

import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.count
import me.plony.bot.extensions.PrivateChannelsExtension
import me.plony.bot.utils.kord

fun PrivateChannelsExtension.delete() = with(kord) {
    on<VoiceStateUpdateEvent> {
        if (old?.channelId in privateChannels) {
            val channel = old?.getChannelOrNull()
            if (channel?.voiceStates?.count() == 0) {
                privateChannels.remove(channel.id)
                channel.delete()
            }
        }
    }
}