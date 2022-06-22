package io.github.seggan.fig.interp.runtime

private val numberPattern = "\\d+(\\.\\d+)?".toRegex()
private val listPattern = "\\[.+(?:,(.+))*]".toRegex()

class InputSource(private val objects: Iterable<Any>) {

    private var idx = 0
    private var iterator = objects.iterator()

    constructor(vararg strings: String) : this(strings.map(::eval))

    fun getInput(): Any {
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
        val result = listPattern.matchEntire(str)
        @Suppress("IfThenToElvis")
        if (result != null) {
            result.groupValues.map(::eval).asSequence()
        } else if (str.startsWith('"') && str.endsWith('"')) {
            str.substring(1, str.length - 1)
        } else {
            str
        }
    }
}