package io.github.seggan.fig.interp.runtime

import java.math.BigDecimal
import java.math.BigInteger

private val regexCache = mutableMapOf<String, Regex>()


fun <T> assertLegalType(obj: T): T {
    if (obj !is BigDecimal && obj !is CallableFunction && obj !is String && obj !is LazyList) {
        throw IllegalArgumentException("Illegal Fig value: '$obj', class: '${obj!!::class.java.name}'")
    }
    return obj
}

fun toBase(n: BigInteger, base: Int): List<Int> {
    val ret = mutableListOf<Int>()
    val b = base.toBigInteger()
    var i = if (n.signum() < 0) n.negate() else n
    while (i > BigInteger.ZERO) {
        ret.add((i % b).toInt())
        i /= b
    }
    ret.reverse()
    if (n.signum() < 0 && ret.isNotEmpty()) {
        ret[0] = -ret[0]
    }
    return ret
}

fun fromBase(n: List<Int>, base: Int): BigInteger {
    val b = base.toBigInteger()
    val i = n.toMutableList()
    if (i[0] < 0) {
        i[0] = -i[0]
    }
    var ret = BigInteger.ZERO
    for (digit in i) {
        ret *= b
        ret += digit
    }
    if (n[0] < 0) {
        ret = ret.negate()
    }
    return ret
}

fun bijectiveBaseEncode(n: BigInteger, base: Int): List<Int> {
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

fun bijectiveBaseDecode(a: List<Int>, base: Int): BigInteger {
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
    return bijectiveBaseEncode(num, codepage.length).map(codepage::get).joinToString("")
}

fun decompress(str: String, codepage: String, cpage: String, dict: List<String>): String {
    val res = mutableListOf<String>()
    var num = bijectiveBaseDecode(str.map(codepage::indexOf), codepage.length)
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

fun figCmp(a: Any, b: Any): Int {
    return if (a is BigDecimal && b is BigDecimal) {
        a.compareTo(b)
    } else if (a is LazyList) {
        if (b is LazyList) {
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
            figCmp(a, listify(b))
        }
    } else if (b is LazyList) {
        figCmp(listify(a), b)
    } else {
        a.asString().compareTo(b.asString())
    }
}

fun figPrint(obj: Any, end: String? = "\n") {
    when (obj) {
        is BigDecimal -> print(obj.stringify())
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
        is BigDecimal -> obj.stringify().filter { it != '.' }.map { it - '0' }.map(Int::toBigDecimal).lazy()
        else -> obj.asString().map(Char::asString).lazy()
    }
}

fun numify(obj: Any): BigDecimal {
    return when (obj) {
        is LazyList -> obj.size.toBigDecimal()
        is String -> obj.toBigDecimalOrNull() ?: obj.length.toBigDecimal()
        is BigDecimal -> obj
        else -> throw IllegalArgumentException("Wrong type for numification: $obj")
    }
}

fun regex(regex: String): Regex {
    return regexCache.getOrPut(regex) { regex.toRegex() }
}

inline fun <reified F, reified S> sortTypesDyadic(a: Any, b: Any): Pair<F, S>? {
    val fClass = F::class.java
    val sClass = S::class.java
    return if (fClass.isInstance(a) && sClass.isInstance(b)) {
        a as F to b as S
    } else if (fClass.isInstance(b) && sClass.isInstance(a)) {
        b as F to a as S
    } else {
        null
    }
}

fun truthiness(obj: Any): Boolean {
    return when (obj) {
        is BigDecimal -> obj != BigDecimal.ZERO
        is LazyList -> obj.isNotEmpty()
        else -> obj.asString().isNotEmpty()
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

fun Boolean.toBigDecimal(): BigDecimal = if (this) BigDecimal.ONE else BigDecimal.ZERO
fun BigDecimal.stringify(): String = this.stripTrailingZeros().toPlainString()

inline fun BigDecimal.applyOnParts(crossinline mappingFunction: (String) -> Any): BigDecimal {
    val split = stringify().split('.')
    return split.map(mappingFunction).joinToString(".", transform = Any::asString).toBigDecimal()
}

/**
 * Same as the [Any.toString] except it uses the [stringify] function on [BigDecimal]s to prevent stringification
 * into scientific notation
 */
fun Any.asString(): String = if (this is BigDecimal) stringify() else toString()

fun <T : Any> Iterable<T>.joinByNothing(): String {
    return joinToString("", transform = Any::asString)
}