package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.example() {
    on<MessageCreateEvent> {
        if (message.author?.isBot == true) return@on
        message.channel.createMessage("Hi")
    }
}