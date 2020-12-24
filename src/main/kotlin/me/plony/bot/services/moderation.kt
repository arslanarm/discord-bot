package me.plony.bot.services

import dev.kord.common.entity.Permission
import dev.kord.core.any
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.*
import me.plony.bot.utils.globals.prefix
import me.plony.bot.utils.shortcuts.messages
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module
fun DiscordReceiver.deletion() {
    messages()
        .filter { it.message.getAuthorAsMember()
            ?.roles
            ?.any { Permission.ManageMessages in it.permissions } == true
        }
        .filter { it.message.content.startsWith("${prefix}удалить сообщения") }
        .map { event -> event.message.mentionedUsers
            .onEach { user -> event.message
                .channel
                .messages
                .filter { it.author == user }
                .collect { it.delete() }
            }
            .launchIn(this)
        }
        .launchIn(this)
}