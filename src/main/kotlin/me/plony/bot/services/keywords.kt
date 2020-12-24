package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.serialization.Serializable
import me.plony.bot.utils.shortcuts.readConfig
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.keywords() {
    @Serializable
    data class Config(val keywords: List<List<String>>, val responses: List<List<String>>)
    val config = readConfig(Config.serializer(), "data/keywords", "config.json")

    on<MessageCreateEvent> {
        if (message.author?.isBot == true) return@on
        val normalizedString = message.content
            .trim()
            .toLowerCase()
        val index = config.keywords
            .indexOfFirst { normalizedString in it }
        if (index == -1) return@on
        val randomPhrase = config.responses[index]
            .random()
            .format(message.author?.mention)
        message.channel.createMessage(randomPhrase)
    }
}