package io.github.seggan.fig.parsing

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.Operator
import io.github.seggan.fig.interp.runtime.CallableFunction
import java.math.BigDecimal

sealed class Node {
    abstract override fun toString(): String
    abstract fun accept(visitor: Interpreter)
}

class ConstantNode(private val obj: Any, private val stringify: (Any) -> String = { "($it)" }) : Node() {
    override fun toString(): String = stringify(obj)

    override fun accept(visitor: Interpreter) = visitor.visitConstant(obj)
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

class LoopNode(private val body: List<Node>) : Node() {
    override fun toString(): String = "((${body.joinToString(" ")}))"
    override fun accept(visitor: Interpreter) = visitor.visitLoop(body)
}

class UnpackNode(val body: Node) : Node() {
    override fun toString(): String = "(u ${body.toString()})"
    override fun accept(visitor: Interpreter) {}
}

class UnpackBulkNode(val body: Node) : Node() {
    override fun toString(): String = "(U ${body.toString()})"
    override fun accept(visitor: Interpreter) {}
}

object NopNode : Node() {
    override fun toString(): String = ""
    override fun accept(visitor: Interpreter) {}
}