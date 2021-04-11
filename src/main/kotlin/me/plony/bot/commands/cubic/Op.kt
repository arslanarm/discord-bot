package me.plony.bot.commands.cubic

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
            else -> throw SyntaxError("Операция '$char' между '$first' и '$second' не определена")
        }
        is Value.Number -> when (second) {
            is Value.Dice -> second.copy(dices = second.dices.map { call(it, first) })
            is Value.Number -> call(first, second)
            else -> throw SyntaxError("Операция '$char' между '$first' и '$second' не определена")
        }
        else -> throw SyntaxError("Операция '$char' между '$first' и '$second' не определена")
    }
}