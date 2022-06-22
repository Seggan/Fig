package io.github.seggan.fig.interp

import io.github.seggan.fig.interp.runtime.InputSource
import io.github.seggan.fig.interp.runtime.figPrint
import io.github.seggan.fig.parsing.Node
import io.github.seggan.fig.parsing.OpNode
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.math.BigDecimal

object Interpreter {

    lateinit var value: Any
    lateinit var inputSource: InputSource

    fun interpret(ast: List<Node>, input: List<String>) {
        inputSource = InputSource(input)
        value = BigDecimal.ZERO
        visit(ast)
        figPrint(value)
    }

    fun visit(node: Node) {
        node.accept(this)
    }

    private fun visit(nodes: List<Node>) {
        nodes.forEach { visit(it) }
    }

    fun visitOp(node: OpNode) {
        val ops = mutableListOf<Any>()
        for (child in node.input) {
            visit(child)
            ops.add(value)
        }
        value = execute(node.operator, ops)
    }

    fun visitConstant(obj: Any) {
        value = obj
    }
}

private val handleCache = mutableMapOf<Operator, MethodHandle>()

private val lookup = MethodHandles.lookup()
private val clazz = Class.forName("io.github.seggan.fig.interp.runtime.RuntimeStuffKt")

private fun execute(op: Operator, operands: List<Any>): Any {
    val handle = handleCache.getOrPut(op) {
        val name = buildString {
            var upper = false
            for (c in op.name) {
                if (c == '_') {
                    upper = true
                } else {
                    if (upper) {
                        append(c.uppercaseChar())
                        upper = false
                    } else {
                        append(c.lowercaseChar())
                    }
                }
            }
        }
        val ins = if (op.arity == -1) {
            arrayOf(List::class.java)
        } else {
            Array(op.arity) { Any::class.java }
        }
        lookup.findStatic(
            clazz, name,
            MethodType.methodType(Any::class.java, ins)
        )
    }
    return handle.invokeWithArguments(if (op.arity == -1) listOf(operands) else operands)
}