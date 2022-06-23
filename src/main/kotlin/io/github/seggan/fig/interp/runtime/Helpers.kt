package io.github.seggan.fig.interp.runtime

import java.math.BigDecimal

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

fun baseEncode(n: Int, base: Int): List<Int> {
    val ret = mutableListOf<Int>()
    var i = n
    while (i > 0) {
        i -= 1
        ret.add(i % base)
        i /= base
    }
    return ret.reversed()
}

fun baseDecode(a: List<Int>, base: Int): Int {
    var n = 0
    for (i in a) {
        n *= base
        n += i + 1
    }
    return n
}

fun decompress(str: String, codepage: String, cpage: String, dict: List<String>): String {
    val res = mutableListOf<String>()
    var num = baseDecode(str.map(codepage::indexOf), codepage.length)
    while (num > 0) {

    }
    return res.reversed().joinToString("")
}