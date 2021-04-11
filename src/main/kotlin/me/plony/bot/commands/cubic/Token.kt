package me.plony.bot.commands.cubic

import java.math.BigDecimal

sealed class Token {
    class Operation(val op: Op) : Token() {
        override fun toString(): String = op.char.toString()
    }
    object Dice : Token() {
        override fun toString(): String = "d"
    }
    object MemoryAccess : Token() {
        override fun toString(): String = "&"
    }
    class NumberLiteral(val value: BigDecimal): Token() {
        override fun toString(): String = value.toString()
    }
    object LeftParenthesis : Token(), Stackable {
        override fun toString(): String = "("
    }
    object RightParenthesis : Token(), Stackable {
        override fun toString(): String = ")"
    }
    class KeyWord(val value: String): Token() {
        override fun toString(): String = value
    }
    class StringLiteral(val value: String): Token(), Stackable {
        override fun toString(): String = "\"$value\""
    }
    class BoolLiteral(val value: Boolean): Token(), Stackable {
        override fun toString(): String = value.toString()
    }
}