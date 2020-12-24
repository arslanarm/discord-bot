package me.plony.bot.utils.shortcuts

import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import me.plony.processor.DiscordReceiver

fun DiscordReceiver.messages() = kord.events
    .filterIsInstance<MessageCreateEvent>()
    .filterNot { it.message.author?.isBot == true }