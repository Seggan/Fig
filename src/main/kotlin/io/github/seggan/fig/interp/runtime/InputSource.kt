package io.github.seggan.fig.interp.runtime

import java.math.BigDecimal

private val numberPattern = "\\d+(\\.\\d+)?".toRegex()

class InputSource(private val objects: Iterable<Any>) {

    private var iterator = objects.iterator()
    private val isEmpty = !iterator.hasNext()

    constructor(strings: List<String>) : this(strings.map(::eval))

    fun getInput(): Any {
        if (isEmpty) return BigDecimal.ZERO
        val ret = iterator.next()
        if (!iterator.hasNext()) {
            iterator = objects.iterator()
        }
        return ret
    }
}

private fun eval(str: String): Any {
    return if (numberPattern.matches(str)) {
        str.toBigDecimal()
    } else {
        if (str.startsWith('[') && str.endsWith(']')) {
            val stripped = str.substring(1, str.length - 1)
            val result = mutableListOf<Any>()
            val builder = StringBuilder()
            var quoting = false
            var brackets = 0
            for (c in stripped) {
                if (c == '"') {
                    quoting = !quoting
                } else if (!quoting) {
                    if (c == '[') {
                        brackets++
                    } else if (c == ']') {
                        brackets--
                    } else if (c == ',' && brackets == 0) {
                        result.add(eval(builder.toString()))
                        builder.clear()
                        continue
                    }
                }
                builder.append(c)
            }
            if (builder.isNotEmpty()) {
                result.add(eval(builder.toString()))
            }
            result.lazy()
        } else if (str.startsWith('"') && str.endsWith('"')) {
            str.substring(1, str.length - 1)
        } else {
            str
        }
    }
}