package me.plony.bot.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.edit

suspend fun MemberBehavior.move(voiceId: Snowflake) = edit {
    voiceChannelId = voiceId
}