package me.plony.bot.utils.shortcuts

import dev.kord.core.entity.Message


suspend fun Message.authorsVoiceState() = getAuthorAsMember()?.getVoiceStateOrNull()
suspend fun Message.respond(message: String) = channel.createMessage(message).let{}