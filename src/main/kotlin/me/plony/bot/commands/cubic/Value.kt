package me.plony.bot.commands.cubic

import java.math.BigDecimal

sealed class Value: Stackable {
    data class Dice(val dices: List<Number>) : Value() {
        constructor(times: Int, max: Int) : this((1..times).map { Number((1..max).random().toBigDecimal()) })

        fun toNumber(): Number = Number(dices.sumOf(Number::number))
        override fun toString() = dices.joinToString(", ", prefix = "D[", postfix = "]")
    }
    class Number(val number: BigDecimal) : Value() {
        override fun toString(): String = number.toString()
    }
    class Bool(val boolean: Boolean): Value() {
        override fun toString(): String = boolean.toString()
    }
}