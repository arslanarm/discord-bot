package me.plony.bot.commands.cubic

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.respond
import kotlinx.coroutines.flow.*


suspend fun Extension.cubic() = command {
    name = "cubic"
    aliases = arrayOf("c", "%")

    action {
        val query = argsList.joinToString(" ")
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

suspend fun main() {
    val query = readLine()!!
    try {
        val result = eval(query)
            .withIndex()
            .map { (i, s) -> "${i + 1}: $s" }
            .toList()
            .joinToString("\n")
        println(result)
    } catch (e: SyntaxError) {
        println("Синтаксическая ошибка ${e.message}")
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
                is Value.Bool -> memory.add(value)
            }

            when (value) {
                is Value.Dice -> emit("$value = ${value.toNumber()}")
                is Value.Number -> emit(value.toString())
                is Value.Bool -> emit(value.toString())
            }
        }
}

private fun evalLine(memory: List<Value>, line: String): Value =
    Parser(Lexer(line), memory)
        .parse()
        .eval()

