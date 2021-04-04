package me.plony.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import me.plony.bot.commands.cubic.cubic

class CubicExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Cubic Extension"
    override suspend fun setup() {
        cubic()
    }
}