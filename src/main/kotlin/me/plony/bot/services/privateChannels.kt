package me.plony.bot.services

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.editMemberPermission
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import me.plony.bot.utils.shortcuts.move
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.privateChannels() {
    @Serializable
    data class Config(
        val guild: String,
        val creatorChannel: String,
        val creatingCategory: String,
        val defaultChannels: List<String>
    )

    val configs = readConfig(ListSerializer(Config.serializer()), "data/privateChannels", "config.json")

    configs.forEach { config ->

        on<ReadyEvent> {
            kord.getGuild(Snowflake(config.guild))!!
                .channels
                .filterIsInstance<VoiceChannel>()
                .filter { it.data.parentId?.value == Snowflake(config.creatingCategory) }
                .onEach {
                    if (it.id.asString !in config.defaultChannels && it.voiceStates.count() == 0)
                        it.delete()
                }
                .collect()
        }
        val privateChannels = mutableListOf<Snowflake>()
        on<VoiceStateUpdateEvent> {
            if (state.channelId?.asString == config.creatorChannel) {
                val userDeferred = async { state.getMember() }
                val guild = state.getGuild()
                val user = userDeferred.await()
                val voice = guild.createVoiceChannel(user.displayName) {
                    parentId = Snowflake(config.creatingCategory)
                }
                privateChannels.add(voice.id)
                launch {
                    voice.editMemberPermission(state.userId) {
                        allowed = Permissions {
                            +Permission.ManageChannels
                        }
                    }
                }
                user.move(voice.id)
            }

            if (old?.channelId in privateChannels) {
                val channel = old?.getChannelOrNull()
                if (channel?.voiceStates?.count() == 0) {
                    channel.delete()
                    privateChannels.remove(channel.id)
                }
            }
        }
    }
}