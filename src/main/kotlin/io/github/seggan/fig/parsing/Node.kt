package io.github.seggan.fig.parsing

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.Operator

sealed interface Node {
    override fun toString(): String
    fun accept(visitor: Interpreter)
}

class StringNode(val value: String) : Node {
    override fun toString(): String = "($value)"
    override fun accept(visitor: Interpreter) = visitor.visitString(value)
}

class NumberNode(val value: Double) : Node {
    override fun toString(): String = "($value)"
    override fun accept(visitor: Interpreter) = visitor.visitNumber(value)
}

class OpNode(val operator: Operator, vararg val input: Node) : Node {
    override fun toString(): String = "($operator $input)"
    override fun accept(visitor: Interpreter) = visitor.visitOp(this)
}