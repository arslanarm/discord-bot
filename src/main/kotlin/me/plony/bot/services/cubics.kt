package me.plony.bot.services

import dev.kord.common.Color
import dev.kord.common.kColor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.plony.bot.utils.api.cubics.Expression
import me.plony.bot.utils.api.cubics.compute
import me.plony.bot.utils.globals.prefix
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on

@Module("Модуль дающий возможность бросить кубики. Команда: <prefix>кубики <expression>")
fun DiscordReceiver.cubics() {
    val localPrefix = "${prefix}кубики"

    on<MessageCreateEvent> {
        if (message.author?.isBot == true || !message.content.toLowerCase().startsWith(localPrefix)) return@on
        val query = message.content
            .toLowerCase()
            .removePrefix(localPrefix)
            .trim()
        if (query.isBlank())
            return@on message.channel.createEmbed {
                description = "Результат: ${Expression.Dice(16).value}"
                color = java.awt.Color.GRAY.kColor
            }.let {}
        try {
            val job = Job()
            val result = compute(query, job) {
                this@cubics.launch {
                    message.channel.createEmbed {
                        title = "Результат ${number}d${max}"
                        color = java.awt.Color.CYAN.kColor
                        description = dices.joinToString(" + ") {
                            it.eval().toString()
                        } + " = ${eval()}"
                    }
                }
            }.eval().toString()
            job.join()
            message.channel.createEmbed {
                title = "Общий результат"
                description = result
                color = Color(0, 255, 0)
            }
        } catch (e: Throwable) {
            message.channel.createEmbed {
                description = "Синтаксическая ошибка"
                color = Color(255, 0, 0)
            }
        }
    }
}