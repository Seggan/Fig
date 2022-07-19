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
        a.asString() + b.asString()
    }
}

fun compress(obj: Any): Any {
    val s = obj.asString()
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
        else -> return obj
    }
}

fun equal(a: Any, b: Any): Any = equalImpl(a, b).toBigDecimal()

private fun equalImpl(a: Any, b: Any): Boolean {
    val intStr = sortTypesDyadic<BigDecimal, String>(a, b)
    if (intStr != null) {
        return intStr.first.stringify() == intStr.second
    } else if (a is BigDecimal && b is BigDecimal) {
        return a.compareTo(b) == 0
    } else if (a is LazyList && b is LazyList) {
        val aIt = a.iterator()
        val bIt = b.iterator()
        while (true) {
            if (!aIt.hasNext()) {
                return !bIt.hasNext()
            } else if (!bIt.hasNext()) {
                return !aIt.hasNext()
            }
            if (!equalImpl(aIt.next(), bIt.next())) {
                return false
            }
        }
    } else {
        return a === b
    }
}

fun generate(a: Any, b: Any): Any {
    val generateArgs = sortTypesDyadic<CallableFunction, Any>(a, b)
    if (generateArgs != null) {
        val (function, arg) = generateArgs
        val list = if (arg is LazyList) arg else listOf(arg)
        val pass = list.reversed().take(function.arity).toMutableList()
        return lazy {
            yieldAll(list)
            while (true) {
                val next = function.call(pass)
                pass.add(next)
                if (pass.size > function.arity) {
                    pass.removeAt(0)
                }
                yield(next)
            }
        }
    } else {
        return maxOf(a, b, ::figCmp)
    }
}

fun index(a: Any, b: Any): Any {
    val invariantArgs = sortTypesDyadic<Any, CallableFunction>(a, b)
    if (invariantArgs != null) {
        val (arg, f) = invariantArgs
        return equal(arg, f.call(arg))
    }
    return if (a is BigDecimal) {
        listify(b)[a.toInt()]
    } else {
        listify(a)[(b as BigDecimal).toInt()]
    }
}

fun input(): Any = Interpreter.inputSource.getInput()

fun isFunction(obj: Any): Any = (obj is CallableFunction).toBigDecimal()

fun isList(obj: Any): Any = (obj is LazyList).toBigDecimal()

fun isNumber(obj: Any) = (obj is BigDecimal).toBigDecimal()

fun isString(obj: Any): Any = (obj is String).toBigDecimal()

fun lastReturnValue(): Any = Interpreter.value

fun map(a: Any, b: Any): Any {
    if (a is CallableFunction && b is CallableFunction) {
        return object : CallableFunction(b.arity) {
            override fun callImpl(inputSource: InputSource): Any {
                return a.call(b.call(inputSource))
            }
        }
    }
    val mapArgs = sortTypesDyadic<Any, CallableFunction>(a, b)
    if (mapArgs != null) {
        val (arg, f) = mapArgs
        if (arg is BigDecimal) {
            return arg.applyOnParts {
                it.map { c -> c - '0' }
                    .map(Int::toBigDecimal)
                    .map(f::call)
                    .joinToString("", transform = Any::asString)
            }
        }
        return listify(arg).map(f::call).toType(arg::class)
    }
    return a
}

fun negate(obj: Any): Any {
    val o = vectorise(::negate, obj)
    if (o != null) return o
    return when (obj) {
        is BigDecimal -> obj.negate()
        is String -> buildString {
            for (c in obj) {
                if (c.isUpperCase()) {
                    append(c.lowercaseChar())
                } else {
                    append(c.uppercaseChar())
                }
            }
        }
        else -> obj
    }
}

fun pair(obj: Any): Any = lazy(obj, obj)

fun printNoNl(obj: Any): Any {
    figPrint(obj, null)
    return obj
}

fun println(obj: Any): Any {
    figPrint(obj)
    return obj
}

fun programInput(): Any = Interpreter.programInput.getInput()

fun remove(a: Any, b: Any): Any {
    if (a is String && b is String) {
        return a + b.substring(a.length)
    }
    val monadicCallArgs = sortTypesDyadic<Any, CallableFunction>(a, b)
    return when {
        monadicCallArgs != null -> monadicCallArgs.second.call(monadicCallArgs.first)
        a is BigDecimal -> a.applyOnParts { it.filter { d -> !equalImpl(d.asString().toBigDecimal(), b) }.asString() }
        a is LazyList -> a.filter { !equalImpl(it, b) }
        else -> a
    }
}

fun sort(obj: Any): Any {
    return when (obj) {
        is LazyList -> obj.sortedWith(::figCmp).lazy()
        is BigDecimal -> obj.applyOnParts { sort(it).asString() }
        is String -> String(obj.toCharArray().sortedArray())
        else -> obj
    }
}

fun sum(obj: Any): Any {
    return when (obj) {
        is LazyList -> {
            if (obj.isEmpty()) {
                BigDecimal.ZERO
            } else {
                val it = obj.iterator()
                var sum = it.next()
                while (it.hasNext()) {
                    sum = add(sum, it.next())
                }
                sum
            }
        }
        is BigDecimal -> obj.applyOnParts { it.sumOf { c -> c - '0' } }
        is String -> obj.chars().sum()
        else -> obj
    }
}

fun thisFunction(): Any = Interpreter.functionStack.first()