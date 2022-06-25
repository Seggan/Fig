package io.github.seggan.fig.parsing

import io.github.seggan.fig.interp.CONSTANTS
import io.github.seggan.fig.interp.Operator
import io.github.seggan.fig.interp.runtime.CallableFunction

class Parser(tokens: List<Token>) {

    private val iterator = tokens.listIterator()

    fun parse(): List<Node> {
        val result = mutableListOf<Node>()
        while (iterator.hasNext()) {
            val token = iterator.next()
            result.add(parseToken(token))
        }
        return result
    }

    private fun parseToken(token: Token): Node {
        return when (token.type) {
            TokenType.STRING -> ConstantNode(token.value)
            TokenType.NUMBER -> ConstantNode(token.value.toBigDecimal())
            TokenType.FUNCTION_REFERENCE -> ConstantNode(CallableFunction(parseToken(iterator.next()))) { "'$it" }
            TokenType.LOOP -> {
                val body = mutableListOf<Node>()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (next.type == TokenType.CLOSER) {
                        break
                    } else {
                        body.add(parseToken(next))
                    }
                }
                LoopNode(body)
            }
            TokenType.OPERATOR -> {
                var operator: Operator? = null
                for (op in Operator.values()) {
                    if (op.symbol == token.value) {
                        operator = op
                        break
                    }
                }
                if (operator == null) {
                    for ((op, v) in CONSTANTS.entries) {
                        if (op == token.value) {
                            return ConstantNode(v)
                        }
                    }
                }
                parseOp(operator ?: throw IllegalArgumentException("Unknown operator: ${token.value}"))
            }
            else -> throw IllegalArgumentException("Unknown token type: ${token.type}")
        }
    }

    private fun parseOp(op: Operator): Node {
        val args = mutableListOf<Node>()
        val arity = op.arity
        if (arity == -2) {
            throw IllegalStateException("Operator $op has no arity")
        }
        if (arity == -1) {
            while (iterator.hasNext()) {
                val token = iterator.next()
                if (token.type == TokenType.CLOSER) {
                    break
                }
                args.add(parseToken(token))
            }
        } else {
            for (i in 0 until op.arity) {
                if (!iterator.hasNext()) {
                    for (j in 0 until (op.arity - i)) {
                        args.add(OpNode(Operator.INPUT))
                    }
                    break
                } else {
                    val token = iterator.next()
                    if (token.type == TokenType.CLOSER) {
                        for (j in 0 until (op.arity - i)) {
                            args.add(OpNode(Operator.INPUT))
                        }
                        break
                    } else {
                        args.add(parseToken(token))
                    }
                }
            }
        }
        return OpNode(op, *args.toTypedArray())
    }
}