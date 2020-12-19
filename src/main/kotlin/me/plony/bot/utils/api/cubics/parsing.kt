package me.plony.bot.utils.api.cubics

import dev.kord.common.kColor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.*
import me.plony.bot.utils.api.cubics.Operation.OperationType.DoubleOperator
import me.plony.bot.utils.api.cubics.Operation.OperationType.SingleOperator
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

val delimiters = listOf(
    SingleOperator.PLUS,
    SingleOperator.MINUS,
    SingleOperator.MULTIPLY,
    SingleOperator.DIVIDE,
    DoubleOperator.PLUS,
    DoubleOperator.MINUS,
    DoubleOperator.MULTIPLY,
    DoubleOperator.DIVIDE,
).reversed()

val stringDelimiters = delimiters
    .map { it.operator }
    .toTypedArray()

fun MessageCreateEvent.compute(s: String, job: CompletableJob): Expression {
    val normalizedString = s.replace(" ", "")
    val elements = normalizedString.split(*stringDelimiters)
    val operations = normalizedString.findOperation(delimiters)
    val (newElements, newOperators) = combineParenthesis(elements, operations)
    return newElements
        .zip(newOperators)
        .fold(Empty as Expression) { acc, (s, op) ->
            val expression = when  {
                s.isBlank() -> Empty
                s.startsWith("(") -> compute(s.drop(1).dropLast(1), Job(job))
                else -> {
                    val doubleValue = s.toDoubleOrNull()
                    if (doubleValue != null)
                        Constant(doubleValue)
                    else {
                        val (amount, max) = s
                            .split("d")
                            .also {
                                require(it.size <= 2)
                            }
                            .map(String::toInt)
                        require(amount > 0 && max > 0)
                        MultipleDice(amount, max).also {
                            GlobalScope.launch(job) { message.channel.createEmbed {
                                title = "Результат ${amount}d${max}"
                                color = Color.CYAN.kColor
                                description = it.dices.joinToString(" + ") {
                                    it.eval().toString()
                                } + " = ${it.eval()}"
                            } }
                        }
                    }
                }
            }
            Operation(acc, expression, op)
        }.also { job.complete() }
}

private fun combineParenthesis(elements: List<String>, ops: List<Operation.OperationType>): Pair<List<String>, List<Operation.OperationType>> {
    var expressionInParenthesis = ""
    var parenthesisDepth = 0
    val newOperators = mutableListOf<Operation.OperationType>()
    val newElements = elements
        .zip((ops.reversed() + SingleOperator.PLUS).reversed())
        .fold(listOf()) { acc: List<String>, (s, op) ->
            if (parenthesisDepth > 0) {
                expressionInParenthesis += "${op.operator}$s"
            }

            val opening = s.takeWhile { it == '(' }.count()
            val closing = s.takeLastWhile { it == ')' }.count()
            when {
                opening == closing && opening > 0 -> {
                    newOperators.add(op)
                    val min = s.takeWhile { it == '(' }.count()
                    acc + s.drop(min).dropLast(min)
                }
                opening > closing -> {
                    parenthesisDepth += opening - closing
                    if (parenthesisDepth == 1) {
                        newOperators.add(op)
                        expressionInParenthesis = s
                    }
                    return@fold acc
                }
                closing > opening -> {
                    parenthesisDepth -= closing - opening
                    if (parenthesisDepth > 0)
                        return@fold acc
                    acc + expressionInParenthesis
                }
                else -> {
                    if (parenthesisDepth > 0)
                        return@fold acc
                    newOperators.add(op)
                    acc + s
                }
            }
        }
    return newElements to newOperators
}

private fun String.findOperation(delimiters: List<Operation.OperationType>): List<Operation.OperationType> {
    val result = mutableListOf<Operation.OperationType>()

    val elements = split(*delimiters.map { it.operator }.toTypedArray())
    var newString = this
    elements.forEach {
        if (!newString.startsWith(it)) {
            val index = newString.indexOf(it)
            val operator = newString.slice(0 until index).trim()
            result.add(delimiters.find { it.operator == operator }!!)
            newString = newString.removePrefix(operator)
        }
        newString = newString.removePrefix(it)
    }

    return result
}
