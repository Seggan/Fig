package io.github.seggan.fig.interp.runtime

import java.math.BigDecimal
import kotlin.reflect.KClass

class LazyList(private val generator: Iterator<Any>) : AbstractCollection<Any>() {

    constructor(sequence: Sequence<Any>) : this(sequence.iterator())

    private val backing = mutableListOf<Any>()

    override val size: Int
        get() = resolve { backing.size }

    override fun isEmpty(): Boolean {
        return backing.isEmpty() && !generator.hasNext()
    }

    operator fun get(index: Int): Any {
        fill(index)
        return backing[index]
    }

    fun hasIndex(index: Int): Boolean {
        fill(index)
        return backing.size > index
    }

    override fun iterator(): Iterator<Any> {
        return object : Iterator<Any> {
            private var i = 0

            override fun hasNext(): Boolean {
                return hasIndex(i)
            }

            override fun next(): Any {
                return get(i++)
            }
        }
    }

    override fun toString(): String {
        fill(20)
        return '[' + backing.joinToString(", ") + (if (generator.hasNext()) "..." else "]")
    }

    fun <T> resolve(andReturn: () -> T): T {
        while (generator.hasNext()) {
            backing.add(generator.next())
        }
        return andReturn()
    }

    fun map(transform: (Any) -> Any): LazyList {
        val it = iterator()
        return LazyList(object : Iterator<Any> {
            override fun hasNext(): Boolean {
                return it.hasNext()
            }
            override fun next(): Any {
                return transform(it.next())
            }
        })
    }
    
    fun toType(clazz: KClass<*>): Any {
        val tClass = clazz.java
        if (LazyList::class.java.isAssignableFrom(tClass)) {
            return this
        } else if (String::class.java == tClass) {
            return joinToString("")
        } else if (BigDecimal::class.java.isAssignableFrom(tClass)) {
            return joinToString("").toBigDecimal()
        } else if (CallableFunction::class.java.isAssignableFrom(tClass)) {
            return object : CallableFunction(0) {
                val it = iterator()
                override fun callImpl(inputSource: InputSource): Any {
                    return if (it.hasNext()) it.next() else BigDecimal.ZERO
                }
            }
        } else {
            throw IllegalArgumentException("Illegal type $tClass")
        }
    }

    private fun fill(upTo: Int) {
        while (generator.hasNext() && backing.size <= upTo) {
            backing.add(generator.next())
        }
    }
}

fun Iterable<Any>.lazy(): LazyList = LazyList(this.iterator())
fun Iterator<Any>.lazy(): LazyList = LazyList(this)
fun lazy(vararg elements: Any): LazyList = LazyList(elements.iterator())
fun lazy(seqGen: suspend SequenceScope<Any>.() -> Unit): LazyList = LazyList(sequence(seqGen))