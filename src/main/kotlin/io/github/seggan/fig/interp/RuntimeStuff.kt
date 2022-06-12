package io.github.seggan.fig.interp

fun add(a: Any, b: Any): Any {
    return if (a is Double && b is Double) {
        a + b
    } else {
        a.toString() + b.toString()
    }
}