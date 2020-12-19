package me.plony.bot.utils.api.cubics

import me.plony.bot.utils.api.cubics.Operation.OperationType.*

sealed class Expression {
    open val value: Expression
        get() = this
    open fun eval(): Double = throw NotImplementedError()
}

object Empty : Expression()

data class Constant(val constant: Double) : Expression() {
    override val value: Expression = this
    override fun eval(): Double = constant
}

data class Dice(val max: Int): Expression() {
    override val value get() = Constant((1..max).random().toDouble())
    override fun eval(): Double = value.eval()
}

data class MultipleDice(
    val number: Int,
    val max: Int,
    val dices: List<Expression> = (1..number).map { Dice(max).value }
): Expression() {
    override val value get() = this
    inline fun withDices(block: (Expression) -> Expression) = copy(dices = dices.map(block))
    override fun eval(): Double = dices.fold<Expression, Expression>(Empty) { acc, dice -> acc + dice.value}.eval()
}

data class Operation(
    private val left: Expression,
    private val right: Expression,
    val operation: OperationType
): Expression() {
    sealed class OperationType(val operator: String) {
        sealed class SingleOperator(operator: String): OperationType(operator) {
            object PLUS : SingleOperator("+")
            object MINUS : SingleOperator("-")
            object MULTIPLY : SingleOperator("*")
            object DIVIDE : SingleOperator("/")
        }
        sealed class DoubleOperator(operator: String): OperationType(operator) {
            object PLUS : DoubleOperator("++")
            object MINUS : DoubleOperator("--")
            object MULTIPLY : DoubleOperator("**")
            object DIVIDE : DoubleOperator("//")
        }
    }

    override val value: Expression
        get()  {
            println(left)
            val lValue = left.value
            val rValue = right.value
            if (lValue is Empty && rValue is Empty) {
                return Empty
            }
            return when (operation) {
                is SingleOperator -> when (operation) {
                    SingleOperator.PLUS -> if (lValue is Empty) rValue.value else Constant(lValue.eval() + rValue.eval())
                    SingleOperator.MINUS -> if (lValue is Empty) Constant(.0)-rValue.value else Constant(lValue.eval() - rValue.eval())
                    SingleOperator.MULTIPLY -> Constant(lValue.eval() * rValue.eval())
                    SingleOperator.DIVIDE -> Constant(lValue.eval() / rValue.eval())
                }

                is DoubleOperator ->
                    when {
                        lValue is MultipleDice && rValue is Constant -> when (operation) {
                            DoubleOperator.PLUS -> lValue + rValue
                            DoubleOperator.MINUS -> lValue - rValue
                            DoubleOperator.MULTIPLY -> lValue * rValue
                            DoubleOperator.DIVIDE -> lValue / rValue
                        }
                        lValue is Constant && rValue is Constant -> when (operation) {
                            DoubleOperator.PLUS -> (lValue + rValue).value
                            DoubleOperator.MINUS -> (lValue - rValue).value
                            DoubleOperator.MULTIPLY -> (lValue * rValue).value
                            DoubleOperator.DIVIDE -> (lValue / rValue).value
                        }
                        lValue is Constant && rValue is MultipleDice -> when(operation){
                            DoubleOperator.PLUS -> lValue + rValue
                            DoubleOperator.MINUS -> lValue - rValue
                            DoubleOperator.MULTIPLY -> lValue * rValue
                            DoubleOperator.DIVIDE -> lValue / rValue
                        }
                        else -> Operation(Constant(left.eval()), Constant(right.eval()), operation)
                    }
            }
        }
    override fun eval(): Double = value.eval()
}

operator fun Expression.plus(other: Expression) = Operation(this, other, SingleOperator.PLUS)
operator fun Expression.minus(other: Expression) = Operation(this, other,  SingleOperator.MINUS)
operator fun Expression.times(other: Expression) = Operation(this, other, SingleOperator.MULTIPLY)
operator fun Expression.div(other: Expression) = Operation(this, other, SingleOperator.DIVIDE)

infix fun Expression.plusplus(other: Expression) = Operation(this, other, DoubleOperator.PLUS)
infix fun Expression.minusminus(other: Expression) = Operation(this, other,  DoubleOperator.MINUS)
infix fun Expression.timestimes(other: Expression) = Operation(this, other, DoubleOperator.MULTIPLY)
infix fun Expression.divdiv(other: Expression) = Operation(this, other, DoubleOperator.DIVIDE)

operator fun MultipleDice.plus(other: Constant) = withDices { it + other }
operator fun MultipleDice.minus(other: Constant) = withDices { it - other }
operator fun MultipleDice.times(other: Constant) = withDices { it * other }
operator fun MultipleDice.div(other: Constant) = withDices { it / other }
operator fun Constant.plus(other: MultipleDice) = other.withDices { this + it }
operator fun Constant.minus(other: MultipleDice) = other.withDices { this - it }
operator fun Constant.times(other: MultipleDice) = other.withDices { this * it }
operator fun Constant.div(other: MultipleDice) = other.withDices { this / it }