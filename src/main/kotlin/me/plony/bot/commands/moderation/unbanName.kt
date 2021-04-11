package me.plony.bot.commands.moderation

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import me.plony.bot.database.BannedName
import me.plony.bot.database.BannedNames
import me.plony.bot.utils.suspendedTransaction

suspend fun Extension.unbanName() = command(::BanNameArgs) {
    name = "unban_name"
    check(::isModerator)
    action {
        val (name) = arguments
        val response = suspendedTransaction {
            BannedName.find { BannedNames.name eq name }
                .firstOrNull()
                ?.delete()
                ?.let { "Имя разбанено" }
                ?: "Имя не является забаненым"
        }
        message.respond(response, useReply = false)
    }
}