package io.github.seggan.fig.interp.runtime

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.Operator
import io.github.seggan.fig.parsing.Node
import io.github.seggan.fig.parsing.OpNode

abstract class CallableFunction(val arity: Int) {

    protected abstract fun callImpl(inputSource: InputSource): Any

    fun call(inputSource: InputSource): Any {
        val oldIn = Interpreter.inputSource
        Interpreter.inputSource = inputSource
        Interpreter.functionStack.addFirst(this)
        val value = callImpl(inputSource)
        Interpreter.functionStack.removeFirst()
        Interpreter.inputSource = oldIn
        return value
    }

    fun call(input: Iterable<Any>): Any {
        return call(InputSource(input))
    }

    fun call(vararg input: Any): Any {
        return call(InputSource(input.toList()))
    }
}

fun countIns(node: OpNode): Int {
    if (node.operator == Operator.INPUT) return 1
    return node.input.filterIsInstance<OpNode>().map(::countIns).sum()
}

class FigFunction(val body: Node, arity: Int = if (body is OpNode) countIns(body) else 0) : CallableFunction(arity) {

    override fun callImpl(inputSource: InputSource): Any {
        Interpreter.visit(body)
        return Interpreter.value
    }
}