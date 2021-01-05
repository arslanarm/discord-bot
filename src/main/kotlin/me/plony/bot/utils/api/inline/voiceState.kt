package me.plony.bot.utils.api.inline

import dev.kord.core.entity.VoiceState

val VoiceState.guild
    get() = guildId.asGuild(kord)

val VoiceState.member
    get() = userId.asMember(kord, guildId)