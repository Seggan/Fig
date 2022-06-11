package io.github.seggan.feg.parsing.tree

import io.github.seggan.feg.parsing.ICompiler

sealed interface Node {
    val children: List<Node>
    override fun toString(): String
    fun accept(visitor: ICompiler)
}