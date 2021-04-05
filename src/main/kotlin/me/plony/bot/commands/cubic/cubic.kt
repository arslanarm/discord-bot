package me.plony.bot.commands.cubic

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.live.channel.live
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.*


suspend fun Extension.cubic() = command {
    name = "cubic"
    aliases = arrayOf("c", "%")

    action {
        val query = args.joinToString(" ")
        try {
            val result = eval(query)
                .withIndex()
                .map { (i, s) -> "${i + 1}: $s" }
                .toList()
                .joinToString("\n")
            message.respond(
                "```json\n$result\n```".trimIndent()
            )
        } catch (e: SyntaxError) {
            message.respond("Синтаксическая ошибка ${e.message}")
        }
    }
}

interface Stackable

sealed class Value: Stackable {
    data class Dice(val dices: List<Number>) : Value() {
        constructor(times: Int, max: Int) : this((1..times).map { Number((1..max).random().toBigDecimal()) })

        fun toNumber(): Number = Number(dices.sumOf(Number::number))
        override fun toString() = dices.joinToString(", ", prefix = "D[", postfix = "]")
    }
    class Number(val number: BigDecimal) : Value() {
        override fun toString(): String = number.toString()
    }
}

private fun eval(query: String): Flow<String> = flow {
    val memory = mutableListOf<Value>()
    query.replace("\n", "")
        .split(";")
        .filterNot(String::isEmpty)
        .forEach {
            val value = evalLine(memory, it)
            when (value) {
                is Value.Dice -> memory.add(value.toNumber())
                is Value.Number -> memory.add(value)
            }

            when (value) {
                is Value.Dice -> emit("$value = ${value.toNumber()}")
                is Value.Number -> emit(value.toString())
            }
        }
}

class SyntaxError(message: String = "") : Exception(message)

enum class Op(val char: Char): Stackable {
    Plus('+') {
        override fun call(first: Value.Number, second: Value.Number) = Value.Number(first.number + second.number)
    },
    Minus('-') {
        override fun call(first: Value.Number, second: Value.Number) = Value.Number(first.number - second.number)
    },
    Times('*') {
        override fun call(first: Value.Number, second: Value.Number) = Value.Number(first.number * second.number)
    },
    Divide('/') {
        override fun call(first: Value.Number, second: Value.Number) = Value.Number(first.number / second.number)
    };
    abstract fun call(first: Value.Number, second: Value.Number): Value.Number
    fun call(first: Value, second: Value): Value = when (first) {
        is Value.Dice -> when (second) {
            is Value.Dice -> throw SyntaxError("Операции между двумя кубиками внутри декларации одного кубика запрещены")
            is Value.Number -> first.copy(dices = first.dices.map { call(it, second) })
        }
        is Value.Number -> when (second) {
            is Value.Dice -> second.copy(dices = second.dices.map { call(it, first) })
            is Value.Number -> call(first, second)
        }
    }
}

private fun evalLine(memory: List<Value>, line: String): Value =
    Parser(Lexer(line), memory)
        .parse()
        .eval()

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
    class Literal(val value: BigDecimal): Token() {
        override fun toString(): String = value.toString()
    }
    object LeftParenthesis : Token(), Stackable {
        override fun toString(): String = "("
    }
    object RightParenthesis : Token(), Stackable {
        override fun toString(): String = ")"
    }
}

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
        return Token.Literal(value)
    }
}

class ASTNode(val op: Op?): Stackable {
    constructor(left: Value, right: Value, op: Op):this(op) {
        leftValue = left
        rightValue = right
    }
    constructor(left: Value, right: ASTNode, op: Op):this(op) {
        leftValue = left
        rightExpression = right
    }
    constructor(left: ASTNode, right: ASTNode, op: Op):this(op) {
        leftExpression = left
        rightExpression = right
    }
    constructor(left: ASTNode, right: Value, op: Op):this(op) {
        leftExpression = left
        rightValue = right
    }
    var leftValue: Value? = null
    var rightValue: Value? = null
    var leftExpression: ASTNode? = null
    var rightExpression: ASTNode? = null
}

fun ASTNode.eval(): Value {
    val left = leftValue ?: leftExpression?.eval()
    val right = rightValue ?: rightExpression?.eval()
    op ?: return left ?: right ?: Value.Number(0.toBigDecimal())
    return op.call(
        left ?: throw SyntaxError("Левый операнд для операции '${op.char}' не найден"),
        right ?: throw SyntaxError("Правый операнд для операции '${op.char}' не найден")
    )
}

class Parser(
    private val lexer: Lexer,
    private val memory: List<Value>
    ) {
    private var lookup = lexer.next()
    fun parse(): ASTNode {
        val stackables = extractStackables()
        val reversePolishNotation = shuntingYard(stackables)
        val stack = Stack<Stackable>()
        for (stackable in reversePolishNotation) {
            when (stackable) {
                is Value -> stack.add(stackable)
                is Op -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    val node = when {
                        right is Value && left is Value -> ASTNode(left, right, stackable)
                        right is Value && left is ASTNode -> ASTNode(left, right, stackable)
                        right is ASTNode && left is ASTNode -> ASTNode(left, right, stackable)
                        right is ASTNode && left is Value -> ASTNode(left, right, stackable)
                        else -> throw SyntaxError("Неизвестный тип как минимум у одного из операндов " +
                                    "при создании AST из обратной польской записи")
                    }
                    stack.add(node)
                }
            }
        }
        return stack.pop().let { when (it) {
            is ASTNode -> it
            is Value -> ASTNode(null).apply { leftValue = it }
            else -> throw SyntaxError("При создании AST в стеке было обнаружено значение неизвестного типа")
        } }
    }

    private fun extractStackables(): List<Stackable> {
        val result = mutableListOf<Stackable>()
        while (lookup != null) {
            when (val next = next()) {
                Token.Dice -> {
                    val times = if (result.lastOrNull() is Value.Number)
                        (result.removeLast() as Value.Number).number.intValueExact()
                    else 1
                    if (lookup !is Token.Literal) throw SyntaxError("При декларации кубика '${times}d$lookup', правое число было объявлено не как число")
                    val max = (next() as Token.Literal).value.intValueExact()
                    if (times !in 1..100 && max !in 1..1000) throw SyntaxError("Значение кубика '${times}d$max' выходят за максимально допустимые значения")
                    result.add(Value.Dice(times, max))
                }
                is Token.Literal -> {
                    result.add(Value.Number(next.value))
                }
                Token.MemoryAccess -> {
                    if (lookup !is Token.Literal) throw SyntaxError("Значение '$lookup' не является валидным значением для получение доступа к памяти")
                    val newNext = (next() as Token.Literal)
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