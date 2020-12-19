package me.plony.bot.utils.api.cubics

import me.plony.bot.utils.api.cubics.Operation.OperationType.*

sealed class Expression {
    open val value: Double
        get() = .0
}

class Constant(override val value: Double) : Expression()

class Dice(max: Int): Expression() {
    override val value = (1..max).random().toDouble()
}

class MultipleDice(
    number: Int,
    private val max: Int
): Expression() {
    val dices = (1..number).map { Dice(max) }
    override val value = dices.sumByDouble { it.value }
}

class Operation(
    private val left: Expression,
    private val right: Expression,
    private val operation: OperationType
): Expression() {
    enum class OperationType(val char: Char) {
        PLUS('+'), MINUS('-'), MULTIPLY('*'), DIVIDE('/')
    }

    override val value: Double
        get() = when (operation) {
            PLUS -> left.value + right.value
            MINUS -> left.value - right.value
            MULTIPLY -> left.value * right.value
            DIVIDE -> left.value / right.value
        }
}

operator fun Expression.plus(other: Expression) = Operation(this, other, PLUS)
operator fun Expression.minus(other: Expression) = Operation(this, other,  MINUS)
operator fun Expression.times(other: Expression) = Operation(this, other, MULTIPLY)
operator fun Expression.div(other: Expression) = Operation(this, other, DIVIDE)