package io.github.seggan.fig.interp.runtime

import java.math.BigDecimal
import java.math.BigInteger

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

fun decompress(str: String, codepage: String, cpage: String, dict: List<String>): String {
    val res = mutableListOf<String>()
    var num = baseDecode(str.map(codepage::indexOf), codepage.length)
    while (num > BigInteger.ZERO) {
        
    }
    return res.reversed().joinToString("")
}