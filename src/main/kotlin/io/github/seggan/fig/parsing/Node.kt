package io.github.seggan.fig.parsing

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.Operator

sealed class Node {
    abstract override fun toString(): String
    abstract fun accept(visitor: Interpreter)
}

class StringNode(val value: String) : Node() {
    override fun toString(): String = "($value)"
    override fun accept(visitor: Interpreter) = visitor.visitString(value)
}

class NumberNode(val value: Double) : Node() {
    override fun toString(): String = "($value)"
    override fun accept(visitor: Interpreter) = visitor.visitNumber(value)
}

class OpNode(val operator: Operator, vararg val input: Node) : Node() {
    override fun toString(): String {
        val symbol = operator.symbol
        return if (input.isEmpty()) {
            "($symbol)"
        } else {
            "($symbol ${input.joinToString(" ")})"
        }
    }

    override fun accept(visitor: Interpreter) = visitor.visitOp(this)
}