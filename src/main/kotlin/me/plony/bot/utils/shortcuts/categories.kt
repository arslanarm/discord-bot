package me.plony.bot.utils.shortcuts

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.Category

suspend fun GuildBehavior.getCategory(id: Snowflake) = getCategoryOrNull(id)!!
suspend fun GuildBehavior.getCategoryOrNull(id: Snowflake) = getChannelOrNull(id) as Category?