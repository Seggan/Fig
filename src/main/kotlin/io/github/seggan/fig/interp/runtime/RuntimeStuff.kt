package io.github.seggan.fig.interp.runtime

import io.github.seggan.fig.interp.Interpreter
import java.math.BigDecimal

fun add(a: Any, b: Any): Any {
    return if (a is BigDecimal && b is BigDecimal) {
        a + b
    } else {
        a.toString() + b.toString()
    }
}

fun eor(obj: Any): Any {
    if (obj is BigDecimal) {
        return LazyList(object : Iterator<Any> {
            private var i = BigDecimal.ONE.negate()
            override fun hasNext(): Boolean = i < obj
            override fun next(): Any {
                i += BigDecimal.ONE
                return i
            }
        })
    }
    throw IllegalArgumentException("Illegal value $obj")
}

fun generate(a: Any, b: Any): Any {
    val pass = (a as LazyList).reversed().take((b as CallableFunction).arity).toMutableList()
    return LazyList(sequence {
        yieldAll(a)
        while (true) {
            val next = b.call(pass)
            pass.add(next)
            if (pass.size > b.arity) {
                pass.removeAt(0)
            }
            yield(next)
        }
    })
}

fun input(): Any {
    return Interpreter.inputSource.getInput()
}

fun pair(obj: Any): Any {
    return LazyList(listOf(obj, obj))
}