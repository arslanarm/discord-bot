import dev.kord.common.entity.PresenceStatus
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.count
import me.plony.bot.utils.globals.prefix
import me.plony.bot.utils.shortcuts.respond

@Module("Модуль дающий возможность посмотреть на онлайн сервера. Команда: %prefix%онлайн")
fun DiscordReceiver.online() {
    val prefix = "${prefix}онлайн"
    on<MessageCreateEvent> {
        if(message.author?.isBot == true || message.content != prefix) return@on

        val guild = getGuild() ?: return@on message.respond("Вы сейчас находитесь не на сервере")
        val onlineMembers = guild.presences
            .count { it.status != PresenceStatus.Offline }

        message.respond("""
                Общее количество участников: ${guild.memberCount}
                Онлайн: $onlineMembers
                """.trimIndent())
    }
}