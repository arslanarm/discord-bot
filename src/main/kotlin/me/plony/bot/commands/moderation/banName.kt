package me.plony.bot.commands.moderation

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.ban
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.on
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import me.plony.bot.database.BannedName
import me.plony.bot.utils.guild
import me.plony.bot.utils.kord
import me.plony.bot.utils.suspendedTransaction

suspend fun Extension.banName()  {
    kord.on<MemberJoinEvent> {
        val bannedNames = suspendedTransaction {
            BannedName.all()
                .map { it.name }
        }
        if (member.username in bannedNames)
            member.ban()
    }

    command(::BanNameArgs) {
        name = "ban_name"
        check(::isModerator)
        action {
            val (name) = arguments
            suspendedTransaction {
                BannedName.new { this.name = name }
            }

            message.respond("Имя добавлено в черный список", useReply = false)
            event.guild!!.withStrategy(EntitySupplyStrategy.rest)
                .members
                .filter { it.username == name }
                .collect { it.ban() }
        }
    }
}

class BanNameArgs : Arguments() {
    val name by string("name", "User to ban")
    operator fun component1() = name
}