package me.plony.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import me.plony.bot.commands.voiceChannelRole.add

class VoiceChannelRoleExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "Voice Channel Role Extension"

    override suspend fun setup() {
        add()
    }
}