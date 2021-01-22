package me.plony.bot.commands.privateChannel

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.*
import me.plony.bot.extensions.PrivateChannelsExtension
import me.plony.bot.utils.kord

fun PrivateChannelsExtension.check() = with(kord) {
    configs.forEach { config ->
        on<ReadyEvent> {
            kord.getGuild(Snowflake(config.guild))!!
                .channels
                .filterIsInstance<VoiceChannel>()
                .filter { it.data.parentId?.value == Snowflake(config.creatingCategory) }
                .onEach {
                    if (it.id.asString !in config.defaultChannels)
                        if (it.voiceStates.count() == 0) {
                            it.delete()
                        } else {
                            privateChannels.add(it.id)
                        }
                }
                .collect()
        }
    }
}