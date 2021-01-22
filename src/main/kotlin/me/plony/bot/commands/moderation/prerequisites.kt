package me.plony.bot.commands.moderation

import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
import dev.kord.core.event.message.MessageCreateEvent

suspend fun Member.isModerator(): Boolean = hasPermission(Permission.BanMembers)
suspend fun isModerator(event: MessageCreateEvent) =
    event.message
        .author
        ?.asMember(event.guildId!!)
        ?.isModerator() ?: false