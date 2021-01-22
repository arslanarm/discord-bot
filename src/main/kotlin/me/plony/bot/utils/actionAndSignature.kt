package me.plony.bot.utils

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.core.event.message.MessageCreateEvent

inline fun <reified T: Arguments> Command.execution(crossinline provider: () -> T, crossinline block: suspend CommandContext.(T) -> Unit) {
    signature { provider() }
    action {
        try {
            block(parse { provider() })
        } catch (e: CloseException) {}
    }
}

class CloseException : Exception()
fun fail(): Nothing = throw CloseException()