package me.plony.bot.commands.privateChannel

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.editMemberPermission
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import me.plony.bot.extensions.PrivateChannelsExtension
import me.plony.bot.utils.asGuild
import me.plony.bot.utils.config
import me.plony.bot.utils.kord
import me.plony.bot.utils.move
import kotlin.time.seconds

fun PrivateChannelsExtension.create() = with(kord) {
    configs.forEach { config ->
        on<VoiceStateUpdateEvent> {
            if (state.channelId?.asString != config.creatorChannel) return@on
            val userDeferred = async { state.getMember() }
            val guild = state.guildId.asGuild(kord)
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
            try {
                withTimeout(2.seconds) {
                    user.move(voice.id)
                }
            } catch (e: Throwable) {
                privateChannels.remove(voice.id)
                voice.delete()
            }
        }
    }
}