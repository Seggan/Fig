package io.github.seggan.fig.interp.runtime

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.Operator
import io.github.seggan.fig.parsing.Node
import io.github.seggan.fig.parsing.OpNode

class CallableFunction(val body: Node) {

    val arity = if (body is OpNode) countIns(body) else 0

    fun call(inputSource: InputSource): Any {
        val prevSource = Interpreter.inputSource
        Interpreter.inputSource = inputSource
        Interpreter.visit(body)
        Interpreter.inputSource = prevSource
        return Interpreter.value
    }

    fun call(input: Iterable<Any>): Any {
        return call(InputSource(input))
    }
}

private fun countIns(node: OpNode): Int {
    if (node.operator == Operator.INPUT) return 1
    return node.input.filterIsInstance<OpNode>().map(::countIns).sum()
}