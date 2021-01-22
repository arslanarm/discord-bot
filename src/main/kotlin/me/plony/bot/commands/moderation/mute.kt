package me.plony.bot.commands.moderation

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.converters.user
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.plony.bot.database.MutedUser
import me.plony.bot.utils.*
import me.plony.bot.utils.DurationConvertor.Companion.duration
import java.time.LocalDateTime
import kotlin.time.seconds
import kotlin.time.toJavaDuration


suspend fun Extension.mute() {
    @Serializable data class Config(val guildId: Long, val roleId: Long)
    val (guildId, roleId) = config(Config.serializer(), "mute").run {
        Snowflake(guildId) to Snowflake(roleId)
    }

    kord.launch {
        do {
            suspendedTransaction {
                MutedUser.all()
                    .asFlow()
                    .filter { it.until.isBefore(LocalDateTime.now()) }
                    .map { it to user(Snowflake(it.userId), kord).asMember(guildId) }
                    .collect { (row, member) ->
                        row.delete()
                        member.removeRole(roleId)
                    }
            }
        } while (delay(10.seconds).returns())
    }

    command {
        name = "mute"

        check(::isModerator)
        execution({ MuteArgs() }) { (user, time) ->
            val member = user.asMember(guildId)
            member.addRole(roleId)
            suspendedTransaction {
                MutedUser.new {
                    userId = user.id.value
                    until = LocalDateTime.now() + time.toJavaDuration()
                }
            }
            message.respond("Участник успешно замучен", useReply = false)
        }
    }
}

class MuteArgs : Arguments() {
    val user by user("user")
    val time by duration("time")

    operator fun component1() = user
    operator fun component2() = time
}