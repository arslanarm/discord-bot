package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import me.plony.bot.utils.globals.prefix
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module("Модуль дающий возможность посмотреть на текущий префикс. Команда: +префикс")
fun DiscordReceiver.prefixShower() {
    on<MessageCreateEvent> {
        if (message.author?.isBot == true || message.content != "+префикс") return@on
        message.channel.createMessage("Нынешний префикс: $prefix")
    }
}