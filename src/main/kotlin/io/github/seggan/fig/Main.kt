package io.github.seggan.fig

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.parsing.Lexer
import io.github.seggan.fig.parsing.Parser
import java.io.File

const val CODEPAGE = "\n !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: java -jar Feg.jar <file>")
        return
    }
    val code = File(args[0]).readText()
    val lexed = Lexer.lex(code)
    println(lexed)
    val parser = Parser(lexed)
    val ast = parser.parse()
    println(ast)
    Interpreter.interpret(ast, args.toList())
}