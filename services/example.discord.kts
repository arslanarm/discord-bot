@file:Description("example", "plony", "0.0.1")
import me.plony.bot.script.Description
import me.plony.bot.script.on
import dev.kord.core.event.message.MessageCreateEvent

on<MessageCreateEvent> {
    if (message.author?.isBot == true) return@on
    message.channel.createMessage("Hello")
}
