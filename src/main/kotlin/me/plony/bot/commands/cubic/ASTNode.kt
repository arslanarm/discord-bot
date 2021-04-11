package me.plony.bot.commands.cubic

sealed class ASTNode: Stackable {
    class ValueNode(val value: Value): ASTNode() {
        override fun eval(): Value = value
    }
    class OpNode(val left: ASTNode, val right: ASTNode, val op: Op): ASTNode() {
        override fun eval(): Value = op.call(left.eval(), right.eval())
    }
    class IfElseNode(val condition: ASTNode, val left: ASTNode, val right: ASTNode): ASTNode() {
        override fun eval(): Value {
            val cond = condition.eval()
            val condBoolean = cond as? Value.Bool
                ?: throw SyntaxError("Условие внтури if должно возвращать тип bool, получено '$cond'")
            return if (condBoolean.boolean)
                left.eval()
            else
                right.eval()
        }
    }

    abstract fun eval(): Value
}