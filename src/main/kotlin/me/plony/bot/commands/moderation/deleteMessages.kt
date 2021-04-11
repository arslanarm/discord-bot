package me.plony.bot.commands.moderation

import com.kotlindiscord.kord.extensions.commands.converters.user
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import me.plony.bot.utils.DurationConvertor.Companion.duration
import kotlin.time.toJavaDuration

suspend fun Extension.deleteMessages() {
    command(::DeleteMessagesArgs) {
        name = "delete_messages"
        check(::isModerator)
        action {
            val (user, time) = arguments
            message.delete()
            message.channel
                .getMessagesBefore(message.id)
                .takeWhile { it.timestamp > message.timestamp - time.toJavaDuration() }
                .filter { it.author == user }
                .collect { it.delete() }
        }
    }
}

class DeleteMessagesArgs : Arguments() {
    val user by user("user", "User, messages of whom will be deleted")
    val time by duration("time", "Time until messages will be deleted")

    operator fun component1() = user
    operator fun component2() = time
}