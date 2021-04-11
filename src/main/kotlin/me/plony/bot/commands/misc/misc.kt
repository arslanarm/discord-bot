package me.plony.bot.commands.misc

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import me.plony.bot.utils.unit
import java.time.ZoneId

suspend fun Extension.misc() {
    command {
        name = "joined_at"

        action {
            val member = getMember()
                ?: return@action channel.createMessage("Участник не найден").unit()
            val time = member.joinedAt.atZone(ZoneId.of("+3")).run {
                "${dayOfMonth.withZeros()}.${monthValue.withZeros()}.$year $hour:$minute"
            }
            message.respond("Вы присоединились на сервер `${getGuild()?.name}` $time")
        }
    }
}

private fun Int.withZeros(): String = if (toString().length < 2) "0" else "" + toString()
