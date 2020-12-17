import com.gitlab.kordlib.common.entity.Status
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.count

@Module
fun DiscordReceiver.online() {

    on<MessageCreateEvent> {
       if(message.author?.isBot == true || message.content != "online") return@on

        val guild = getGuild()!!
        val onlineMembers = guild.members
            .count { (it.getPresenceOrNull()?.status ?: Status.Offline) != Status.Offline }

        message.channel.createMessage("""
                Общее количество участников: ${guild.memberCount}
                Онлайн: $onlineMembers
                """.trimIndent())
    }
}