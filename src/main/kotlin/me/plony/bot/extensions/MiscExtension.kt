package me.plony.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import me.plony.bot.commands.misc.misc

class MiscExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Misc Extension"

    override suspend fun setup() {
        misc()
    }
}