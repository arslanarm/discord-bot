import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import com.gitlab.kordlib.core.event.message.MessageCreateEvent

@Module
fun DiscordReceiver.online() {
    on<MessageCreateEvent>(){
       if(message.content == "online"){
        val guild = getGuild()!!
        message.channel.createMessage(guild.memberCount.toString())
       }
    }
}