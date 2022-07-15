package io.github.seggan.fig.interp

import io.github.seggan.fig.interp.runtime.CallableFunction
import io.github.seggan.fig.interp.runtime.InputSource
import io.github.seggan.fig.interp.runtime.LazyList
import io.github.seggan.fig.interp.runtime.figPrint
import io.github.seggan.fig.interp.runtime.listify
import io.github.seggan.fig.interp.runtime.truthiness
import io.github.seggan.fig.parsing.IfNode
import io.github.seggan.fig.parsing.Node
import io.github.seggan.fig.parsing.OpNode
import io.github.seggan.fig.parsing.UnpackBulkNode
import io.github.seggan.fig.parsing.UnpackNode
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.math.BigDecimal

object Interpreter {

    var value: Any = BigDecimal.ZERO
        set(value) {
            if (
                value !is BigDecimal
                && value !is CallableFunction
                && value !is String
                && value !is LazyList
            ) {
                throw IllegalArgumentException("Illegal Fig value: '$value', class: '${value::class.java.name}'")
            }
            field = value
        }
    lateinit var inputSource: InputSource
    lateinit var programInput: InputSource
    val functionStack = ArrayDeque<CallableFunction>()

    fun interpret(ast: List<Node>, input: List<String>) {
        inputSource = InputSource(input)
        programInput = inputSource
        functionStack.addFirst(object : CallableFunction(input.size) {
            override fun callImpl(inputSource: InputSource): Any {
                visit(ast)
                return value
            }
        })
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
            if (child is UnpackBulkNode) {
                val iterations = node.operator.arity - ops.size
                visit(child.body)
                for (i in 0 until iterations) {
                    ops.add(value)
                }
                break
            } else if (child is UnpackNode) {
                val iterations = node.operator.arity - ops.size
                visit(child.body)
                val it = listify(value).iterator()
                for (i in 0 until iterations) {
                    if (it.hasNext()) {
                        ops.add(it.next())
                    } else {
                        break
                    }
                }
            } else {
                ops.add(value)
            }
        }
        value = execute(node.operator, ops)
    }

    fun visitConstant(obj: Any) {
        value = obj
    }

    fun visitLoop(body: List<Node>) {
        while (true) {
            visit(body)
        }
    }

    fun visitIf(node: IfNode) {
        visit(node.cond)
        if (truthiness(value)) {
            visit(node.then)
        } else if (node.otherwise != null) {
            visit(node.otherwise)
        }
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