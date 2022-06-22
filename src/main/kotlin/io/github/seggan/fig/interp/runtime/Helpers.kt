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