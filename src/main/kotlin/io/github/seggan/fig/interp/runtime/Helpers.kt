package io.github.seggan.fig.interp.runtime

import java.lang.invoke.MethodHandle
import java.math.BigDecimal
import java.math.BigInteger

fun baseEncode(n: BigInteger, base: Int): List<Int> {
    val ret = mutableListOf<Int>()
    val b = base.toBigInteger()
    var i = n
    while (i > BigInteger.ZERO) {
        i -= BigInteger.ONE
        ret.add((i % b).toInt())
        i /= b
    }
    return ret.reversed()
}

fun baseDecode(a: List<Int>, base: Int): BigInteger {
    val b = base.toBigInteger()
    var n = BigInteger.ZERO
    for (i in a) {
        n *= b
        n += (i + 1).toBigInteger()
    }
    return n
}

fun compress(str: String, codepage: String, cpage: String, dict: List<String>): String {
    var num = BigInteger.ZERO
    val string = StringBuilder(str)
    val orderedDict = dict.sortedBy { -it.length }
    while (string.isNotEmpty()) {
        val isSpace = string[0] == ' '
        if (isSpace) {
            string.deleteCharAt(0)
        }
        val c = string[0]
        val isUpper = c.isUpperCase()
        string.deleteCharAt(0)
        string.insert(0, c.lowercaseChar())
        var isWord = false
        for (word in orderedDict) {
            if (string.startsWith(word)) {
                isWord = true
                string.delete(0, word.length)
                num *= dict.size
                num += dict.indexOf(word)
                break
            }
        }
        if (!isWord) {
            num *= cpage.length
            num += cpage.indexOf(c)
            string.deleteCharAt(0)
        }
        var tag = isWord.toInt() shl 2
        tag += isSpace.toInt() shl 1
        tag += isUpper.toInt()
        num *= 8
        num += tag
    }
    return baseEncode(num, codepage.length).map(codepage::get).joinToString("")
}

fun decompress(str: String, codepage: String, cpage: String, dict: List<String>): String {
    val res = mutableListOf<String>()
    var num = baseDecode(str.map(codepage::indexOf), codepage.length)
    while (num > BigInteger.ZERO) {
        val tag = num % 8
        num /= 8
        if (tag and 0b100 == 0) {
            var c = cpage[num % cpage.length]
            num /= cpage.length
            if (tag and 0b001 > 0) {
                c = c.uppercaseChar()
            }
            res.add(c.toString())
        } else {
            val idx = num % dict.size
            num /= dict.size
            var word = dict[idx]
            if (tag and 0b001 > 0) {
                word = word[0].uppercaseChar() + word.substring(1)
            }
            res.add(word)
        }
        if (tag and 0b010 > 0) {
            res.add(" ")
        }
    }
    return res.reversed().joinToString("")
}

fun figPrint(obj: Any, end: String? = "\n") {
    when (obj) {
        is BigDecimal -> print(obj.stripTrailingZeros().toPlainString())
        is LazyList -> {
            print('[')
            val it = obj.iterator()
            while (it.hasNext()) {
                figPrint(it.next(), null)
                if (it.hasNext()) {
                    print(", ")
                }
            }
            print(']')
        }
        else -> print(obj)
    }
    if (end != null) print(end)
}

fun listify(obj: Any): LazyList {
    return when (obj) {
        is LazyList -> obj
        is BigDecimal -> obj.stripTrailingZeros().toPlainString().map { it - '0' }.lazy()
        else -> obj.toString().map(Char::toString).lazy()
    }
}

fun vectorise(function: (Any) -> Any, arg: Any): Any? {
    if (arg is LazyList) {
        return arg.map(function)
    }
    return null
}

fun vectorise(function: (Any, Any) -> Any, arg1: Any, arg2: Any): Any? {
    if (arg1 is LazyList) {
        if (arg2 is LazyList) {
            val it1 = arg1.iterator()
            val it2 = arg2.iterator()
            return object : Iterator<Any> {
                override fun hasNext(): Boolean {
                    return it1.hasNext() && it2.hasNext()
                }
                override fun next(): Any {
                    return function(it1.next(), it2.next())
                }
            }.lazy()
        }
        return arg1.map { function(it, arg2) }
    } else if (arg2 is LazyList) {
        return arg2.map { function(arg1, it) }
    }
    return null
}

private fun Boolean.toInt(): Int = if (this) 1 else 0

private operator fun BigInteger.div(i: Int): BigInteger = this / i.toBigInteger()
private operator fun BigInteger.rem(i: Int): Int = (this % i.toBigInteger()).toInt()
private operator fun BigInteger.plus(i: Int): BigInteger = this + i.toBigInteger()
private operator fun BigInteger.times(i: Int): BigInteger = this * i.toBigInteger()
