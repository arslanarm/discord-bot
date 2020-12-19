package me.plony.bot.utils.api.cubics

import dev.kord.common.kColor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.runBlocking
import java.awt.Color

fun MessageCreateEvent.compute(s: String): Double {
    var parenthesisDepth = 0
    var expressionInParenthesis = ""
    val normalizedString = s.replace(" ", "")
        .toCharArray()
        .joinToString("") {
            when (it) {
                '(' -> {
                    parenthesisDepth++
                    ""
                }
                ')' -> {
                    parenthesisDepth--
                    require(parenthesisDepth >= 0)
                    if (parenthesisDepth == 0) {
                        compute(expressionInParenthesis.also { expressionInParenthesis = "" }).toString()
                    } else ""
                }
                else -> {
                    if (parenthesisDepth > 0) {
                        expressionInParenthesis += it
                        ""
                    } else it.toString()
                }
            }
        }
    val delimiters = Operation.OperationType.values()
        .map { it.char }.toCharArray()

    val operations = normalizedString.toCharArray()
        .filter { it in delimiters }
        .map { chr -> Operation.OperationType
            .values()
            .first { it.char == chr}
        }
    val elements = normalizedString.split(*delimiters)
    return elements.withIndex().fold<IndexedValue<String>, Expression>(Constant(.0)) { acc, (index, str) ->
        val doubleValue = str.toDoubleOrNull()
        val expression = if (doubleValue != null) {
            Constant(doubleValue)
        } else {
            val (number, max) = str.split("d").map { if (it.isBlank()) 1 else it.toInt() }
            MultipleDice(number, max).also {
                runBlocking { message.channel.createEmbed {
                    title = "Результат $str"
                    description = "${it.dices.joinToString(" + ") { it.value.toString() }} = ${it.value}"
                    color = Color.CYAN.kColor
                } }
            }
        }
        if (index == 0) {
            return@fold acc + expression
        }
        when (operations[index - 1]) {
            Operation.OperationType.PLUS -> acc + expression
            Operation.OperationType.MINUS -> acc - expression
            Operation.OperationType.MULTIPLY -> acc * expression
            Operation.OperationType.DIVIDE -> acc / expression
        }
    }.value
}