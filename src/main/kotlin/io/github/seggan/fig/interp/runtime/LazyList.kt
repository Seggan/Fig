package io.github.seggan.fig.interp.runtime

class LazyList(private val generator: Iterator<Any>) : AbstractCollection<Any>() {

    constructor(iterable: Iterable<Any>) : this(iterable.iterator())
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

    private fun fill(upTo: Int) {
        while (generator.hasNext() && backing.size <= upTo) {
            backing.add(generator.next())
        }
    }
}

fun Iterable<Any>.lazy(): LazyList = LazyList(this.iterator())
fun Sequence<Any>.lazy(): LazyList = LazyList(this.iterator())
fun Iterator<Any>.lazy(): LazyList = LazyList(this)