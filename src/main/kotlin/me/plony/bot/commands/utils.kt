package me.plony.bot.commands

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.entity.Member
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import me.jakejmattson.discordkt.api.dsl.commands
import me.plony.bot.config.Config
import me.plony.bot.config.Messages
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

fun utils() = commands("Utils") {
    command("joined_at") {
        execute {
            val member = author.asMember(guild?.id ?: return@execute)
            val joinedAt = member.joinedAt()
            respond(Config.messages.joinedAt.format(joinedAt))
        }
    }
    command("online") {
        execute {
            val guild = guild!!
            val (total, online) = coroutineScope {
                guild.withStrategy(EntitySupplyStrategy.rest)
                    .members
                    .map { async { it.getPresenceOrNull() } }
                    .map { it.await() }
                    .fold(0 to 0) { (total, online), value ->
                        total + 1 to online + if (value?.status == PresenceStatus.Online) 1 else 0
                    }
            }
            respond("Общее кол-во участников: $total.\nОнлайн: $online")
        }
    }
}

private fun Member.joinedAt(): String {
    val dateTime = joinedAt
        .toLocalDateTime(TimeZone.UTC)
        .toJavaLocalDateTime()
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale("ru"))
    return formatter.format(dateTime)
}