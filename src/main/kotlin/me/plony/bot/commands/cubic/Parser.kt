package me.plony.bot.commands.cubic

import java.math.BigDecimal
import java.util.*

class Parser(
    private val lexer: Lexer,
    private val memory: List<Value>
    ) {
    private var lookup = lexer.next()
    fun parse(): ASTNode {
        val stackables = extractStackables()
        val reversePolishNotation = shuntingYard(stackables)
        val stack = Stack<ASTNode>()
        for (stackable in reversePolishNotation) {
            when (stackable) {
                is Value -> stack.add(ASTNode.ValueNode(stackable))
                is Op -> {
                    val right = stack.pop()
                    val left = stack.pop()

                    stack.add(ASTNode.OpNode(left, right, stackable))
                }
            }
        }
        return stack.pop()
    }

    private fun extractStackables(): List<Stackable> {
        val result = mutableListOf<Stackable>()
        while (lookup != null) {
            when (val next = next()) {
                Token.Dice -> {
                    val times = if (result.lastOrNull() is Value.Number)
                        (result.removeLast() as Value.Number).number
                    else BigDecimal(1)
                    if (lookup !is Token.NumberLiteral) throw SyntaxError("При декларации кубика '${times}d$lookup', правое число было объявлено не как число")
                    val max = (next() as Token.NumberLiteral).value
                    if (!(BigDecimal(1) <= times && times <= BigDecimal(100)) ||
                        !(BigDecimal(1) <= max && max <= BigDecimal(1000))) throw SyntaxError("Значение кубика '${times}d$max' выходят за максимально допустимые значения")
                    result.add(Value.Dice(times.intValueExact(), max.intValueExact()))
                }
                is Token.NumberLiteral -> {
                    result.add(Value.Number(next.value))
                }
                Token.MemoryAccess -> {
                    if (lookup !is Token.NumberLiteral) throw SyntaxError("Значение '$lookup' не является валидным значением для получение доступа к памяти")
                    val newNext = (next() as Token.NumberLiteral)
                    result.add(memory.getOrNull(newNext.value.intValueExact() - 1) ?: throw SyntaxError("Значение '$newNext' выходят за допустимые значения для получения доступа к памяти"))
                }
                is Token.Operation -> {
                    result.add(next.op)
                }
                Token.LeftParenthesis -> {
                    result.add(Token.LeftParenthesis)
                }
                Token.RightParenthesis -> {
                    result.add(Token.RightParenthesis)
                }
                is Token.StringLiteral -> {
                    result.add(next)
                }
                is Token.KeyWord -> {

                }
                null -> break
            }
        }
        return result
    }

    private fun shuntingYard(stackables: List<Stackable>): List<Stackable> {
        val output = mutableListOf<Stackable>()
        val stack = Stack<Stackable>()
        for (stackable in stackables) {
            when (stackable) {
                is Value -> output.add(stackable)
                is Op -> {
                    while (stack.isNotEmpty() && stack.peek().let { it is Op && it.precedence >= stackable.precedence }) {
                        output.add(stack.pop())
                    }
                    stack.push(stackable)
                }
                is Token.LeftParenthesis -> stack.add(stackable)
                is Token.RightParenthesis -> {
                    while (stack.peek() != Token.LeftParenthesis) {
                        output.add(stack.pop())
                    }
                    stack.pop()
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.pop())
        }
        return output
    }

    fun next(): Token? = lookup.also { lookup = lexer.next() }
}

private val Op.precedence: Int get() = when (this) {
    Op.Plus -> 2
    Op.Minus -> 2
    Op.Times -> 3
    Op.Divide -> 3
}