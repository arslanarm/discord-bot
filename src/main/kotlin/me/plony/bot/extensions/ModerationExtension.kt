package me.plony.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import me.plony.bot.commands.moderation.banName
import me.plony.bot.commands.moderation.deleteMessages
import me.plony.bot.commands.moderation.mute
import me.plony.bot.commands.moderation.unbanName

class ModerationExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Moderation Extension"

    override suspend fun setup() {
        banName()
        mute()
        deleteMessages()
        unbanName()
    }
}
