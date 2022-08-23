package io.github.seggan.fig.parsing

import ch.obermuhlner.math.big.BigDecimalMath
import io.github.seggan.fig.interp.CONSTANTS
import io.github.seggan.fig.interp.Operator
import io.github.seggan.fig.interp.runtime.FigFunction
import java.math.BigDecimal
import java.math.MathContext

class Parser(tokens: List<Token>) {

    private val iterator = tokens.listIterator()

    private var isEnding = false

    fun parse(): List<Node> {
        val result = mutableListOf<Node>()
        while (iterator.hasNext()) {
            isEnding = false
            result.add(parseToken(iterator.next()))
        }
        return result
    }

    private fun parseToken(token: Token): Node {
        return when (token.type) {
            TokenType.STRING -> ConstantNode(token.value)
            TokenType.NUMBER -> ConstantNode(token.value.toBigDecimal())
            TokenType.FUNCTION_REFERENCE -> {
                val node = ConstantNode(FigFunction(parseToken(iterator.next()))) { "'$it" }
                isEnding = false
                node
            }
            TokenType.OPERATOR_REFERENCE -> {
                val next = iterator.next()
                if (next.type == TokenType.NUMBER) {
                    return ConstantNode(BigDecimalMath.pow(BigDecimal.TEN, next.value.toBigDecimal(), MathContext.DECIMAL128)) { "'$it" }
                }
                val operator = Operator.values().find { it.symbol == next.value }!!
                ConstantNode(FigFunction(OpNode(operator, List(operator.arity) { OpNode(Operator.INPUT) }))) { "@$it" }
            }
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
                val operator = Operator.values().find { it.symbol == token.value }
                if (operator == null) {
                    for ((op, v) in CONSTANTS.entries) {
                        if (op == token.value) {
                            return ConstantNode(v)
                        }
                    }
                }
                parseOp(operator ?: throw IllegalArgumentException("Unknown operator: ${token.value}"))
            }
            else -> NopNode
        }
    }

    private fun parseOp(op: Operator): OpNode {
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
                } else if (token.type == TokenType.UNPACK) {
                    args.add(UnpackNode(parseToken(iterator.next())))
                    break
                } else if (token.type == TokenType.UNPACK_BULK) {
                    args.add(UnpackBulkNode(parseToken(iterator.next())))
                } else {
                    args.add(parseToken(token))
                }
            }
        } else {
            var i = 0
            while (i < op.arity) {
                if (!iterator.hasNext() || isEnding) {
                    for (j in 0 until (op.arity - i)) {
                        args.add(OpNode(Operator.INPUT))
                    }
                    break
                } else {
                    val token = iterator.next()
                    if (token.type == TokenType.CLOSER || isEnding) {
                        for (j in 0 until (op.arity - i)) {
                            args.add(OpNode(Operator.INPUT))
                        }
                        break
                    } else {
                        when (token.type) {
                            TokenType.UNPACK -> args.add(UnpackNode(parseToken(iterator.next())))
                            TokenType.UNPACK_BULK -> args.add(UnpackBulkNode(parseToken(iterator.next())))
                            TokenType.END_FUNCTION -> isEnding = i-- > -1 // setting it to true. don't try this at home
                            else -> args.add(parseToken(token))
                        }
                    }
                }
                i++
            }
        }
        return OpNode(op, args)
    }
}