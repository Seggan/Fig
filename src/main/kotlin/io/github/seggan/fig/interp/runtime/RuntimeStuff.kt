@file:Suppress("unused")

package io.github.seggan.fig.interp.runtime

import io.github.seggan.fig.COMPRESSABLE_CHARS
import io.github.seggan.fig.COMPRESSION_CODEPAGE
import io.github.seggan.fig.DICTIONARY
import io.github.seggan.fig.interp.Interpreter
import java.math.BigDecimal

fun add(a: Any, b: Any): Any {
    return if (a is BigDecimal && b is BigDecimal) {
        a + b
    } else {
        a.toString() + b.toString()
    }
}

fun compress(obj: Any): Any {
    val s = obj.toString()
    val compressed = compress(s, COMPRESSION_CODEPAGE, COMPRESSABLE_CHARS, DICTIONARY)
    return if (compressed.length < s.length) "@$compressed\"" else "\"$s\""
}

fun eor(obj: Any): Any {
    when (obj) {
        is BigDecimal -> {
            return object : Iterator<Any> {
                private var i = BigDecimal.ONE.negate()
                override fun hasNext(): Boolean = i < obj
                override fun next(): Any {
                    i += BigDecimal.ONE
                    return i
                }
            }.lazy()
        }
        is LazyList -> {
            if (obj.isEmpty()) {
                return obj
            }
            return object : Iterator<Any> {
                private var it = obj.iterator()
                override fun hasNext(): Boolean = true
                override fun next(): Any {
                    if (!it.hasNext()) {
                        it = obj.iterator()
                    }
                    return it.next()
                }
            }.lazy()
        }
        else -> {
            throw IllegalArgumentException("Cannot eor $obj")
        }
    }
}

fun generate(a: Any, b: Any): Any {
    if (b is CallableFunction) {
        val list = if (a is LazyList) a else listOf(a)
        val pass = list.reversed().take(b.arity).toMutableList()
        return sequence {
            yieldAll(list)
            while (true) {
                val next = b.call(pass)
                pass.add(next)
                if (pass.size > b.arity) {
                    pass.removeAt(0)
                }
                yield(next)
            }
        }.lazy()
    } else {
        return figCmp(a, b)
    }
}

fun index(a: Any, b: Any): Any {
    return if (a is BigDecimal) {
        listify(b)[a.toInt()]
    } else {
        listify(a)[(b as BigDecimal).toInt()]
    }
}

fun input(): Any = Interpreter.inputSource.getInput()

fun lastReturnValue(): Any = Interpreter.value

fun pair(obj: Any): Any = listOf(obj, obj).lazy()

fun printNoNl(obj: Any): Any {
    figPrint(obj, null)
    return obj
}

fun println(obj: Any): Any {
    figPrint(obj)
    return obj
}

fun programInput(): Any = Interpreter.programInput.getInput()

private fun figCmp(a: Any, b: Any): Int {
    return if (a is BigDecimal && b is BigDecimal) {
        a.compareTo(b)
    } else if (a is LazyList && b is LazyList) {
        val aIt = a.iterator()
        val bIt = b.iterator()
        var cmp: Int
        while (true) {
            if (!aIt.hasNext()) {
                return if (bIt.hasNext()) -1 else 0
            }
            if (!bIt.hasNext()) {
                return if (aIt.hasNext()) 1 else 0
            }
            val aNext = aIt.next()
            val bNext = bIt.next()
            cmp = figCmp(aNext, bNext)
            if (cmp != 0) {
                break
            }
        }
        cmp
    } else {
        a.toString().compareTo(b.toString())
    }
}