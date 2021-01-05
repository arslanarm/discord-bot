package me.plony.bot.services

import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.util.*
import io.ktor.util.date.*
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import java.time.Instant
import java.time.temporal.ChronoUnit

@Module
fun DiscordReceiver.joinedAt() {
    on<MessageCreateEvent> {
        if (!message.content.toLowerCase().startsWith("луна когда я зашел на сервер")) return@on
        val guild = getGuild() ?: return@on
        val member = message.getAuthorAsMember() ?: return@on
        message.channel.createMessage("Вы зашли на сервер ${guild.name} в ${member.joinedAt.truncatedTo(ChronoUnit.MINUTES).string()}")
    }
}

private fun Instant.string(): String = with(toGMTDate().toJvmDate().toLocalDateTime()) {
    "$dayOfMonth/$monthValue/$year $hour:$minute"
}
