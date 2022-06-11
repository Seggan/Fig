package io.github.seggan.feg

import io.github.seggan.feg.parsing.Lexer
import java.io.File

const val CODEPAGE = "\t\n !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: java -jar Feg.jar <file>")
        return
    }
    val code = File(args[0]).readText()
    val lexed = Lexer.lex(code)
}