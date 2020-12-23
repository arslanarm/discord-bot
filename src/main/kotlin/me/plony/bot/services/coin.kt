package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import me.plony.bot.utils.globals.prefix
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module("Модуль дающий возможность бросить монетку. Команда: %prefix%монетка")
fun DiscordReceiver.coin(){
    on<MessageCreateEvent> {
        if (message.author?.isBot == true || message.content != "${prefix}монетка") return@on
        val response = listOf("Решка", "Орел").random()
        message.channel.createMessage("Выпал(а) $response")
    }
}