package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.coin(){
    on<MessageCreateEvent> {
        if (message.content == "подбрось монетку"){
            val response = listOf("Решка", "Орел").random()
            message.channel.createMessage("Выпал(а) $response")
        }

    }

}