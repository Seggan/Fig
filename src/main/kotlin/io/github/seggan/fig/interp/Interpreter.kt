package io.github.seggan.fig.interp

import io.github.seggan.fig.parsing.Node
import io.github.seggan.fig.parsing.OpNode
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

object Interpreter {

    private lateinit var value: Any

    fun interpret(ast: List<Node>) {
        visit(ast)
        println(value)
    }

    private fun visit(node: Node) {
        node.accept(this)
    }

    private fun visit(nodes: List<Node>) {
        nodes.forEach { visit(it) }
    }

    fun visitString(str: String) {
        value = str
    }

    fun visitOp(node: OpNode) {
        val ops = mutableListOf<Any>()
        for (child in node.input) {
            visit(child)
            ops.add(value)
        }
        value = execute(node.operator, ops)
    }

    fun visitNumber(num: Double) {
        value = num
    }
}

private val handleCache = mutableMapOf<Operator, MethodHandle>()

private val lookup = MethodHandles.lookup()
private val clazz = Class.forName("io.github.seggan.fig.interp.RuntimeStuffKt")

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