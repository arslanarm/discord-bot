package me.plony.bot.commands.cubic

import java.math.BigDecimal

class Lexer(val line: String) : Iterator<Token?> {
    private var cursor = 0
    override fun hasNext(): Boolean = cursor < line.length
    override fun next(): Token? {
        while (hasNext()) {
            val lineLeft = line.drop(cursor).trimStart()
            cursor = line.length - lineLeft.length
            when {
                lineLeft.startsWith("//") -> {
                    cursor = line.length
                    continue
                }
                lineLeft.startsWith("/*") -> {
                    cursor += lineLeft.indexOf("*/") + 2
                    continue
                }
            }
            return when (lineLeft.first()) {
                ' ' -> continue
                in '0'..'9' -> parseNumberLiteral(lineLeft)
                'd' -> Token.Dice.also { cursor++ }
                '&' -> Token.MemoryAccess.also { cursor++ }
                '(' -> Token.LeftParenthesis.also { cursor++ }
                ')' -> Token.RightParenthesis.also { cursor++ }
                in Op.values().map(Op::char) -> Token.Operation(Op.values().first { it.char == lineLeft.first() }).also { cursor++ }
                else -> throw SyntaxError("Неизвестный символ ${lineLeft.first()}")
            }
        }
        return null
    }

    private fun parseNumberLiteral(line: String): Token {
        var value = BigDecimal(0)
        for (c in line) {
            when (c) {
                in '0'..'9' -> value = value * 10.toBigDecimal() + BigDecimal(c - '0')
                else -> break
            }
            cursor++
        }
        return Token.NumberLiteral(value)
    }
}