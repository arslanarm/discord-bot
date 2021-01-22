package me.plony.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import me.plony.bot.commands.privateChannel.check
import me.plony.bot.commands.privateChannel.create
import me.plony.bot.commands.privateChannel.delete
import me.plony.bot.utils.config

class PrivateChannelsExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Private Channels Extension"
    val configs = config(ListSerializer(Config.serializer()), "privateChannels")
    val privateChannels = mutableListOf<Snowflake>()
    override suspend fun setup() {
        check()
        create()
        delete()
    }

    @Serializable
    data class Config(
        val guild: String,
        val creatorChannel: String,
        val creatingCategory: String,
        val defaultChannels: List<String>
    )
}