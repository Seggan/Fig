package io.github.seggan.fig

import io.github.seggan.fig.interp.Interpreter
import io.github.seggan.fig.interp.runtime.bijectiveBaseDecode
import io.github.seggan.fig.interp.runtime.bijectiveBaseEncode
import io.github.seggan.fig.parsing.Lexer
import io.github.seggan.fig.parsing.Parser
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.math.MathContext
import java.math.RoundingMode
import ch.obermuhlner.math.big.BigDecimalMath as BDM

const val CODEPAGE =
    "\n !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
const val COMPRESSION_CODEPAGE =
    " !#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
const val COMPRESSABLE_CHARS = "abcdefghijklmnopqrstuvwxyz\n0123456789'()*+,-. !\"#$%&/:;<=>?@[\\]^_`{|}~"
val DICTIONARY = object {}.javaClass.getResource("/dict.txt")!!.readText().split("\n")

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: java -jar Fig.jar <command> <file> [args]")
        return
    }
    when (args[0]) {
        "executeUTF8" -> {
            val code = File(args[1]).readText()
            val lexed = Lexer.lex(code)
            val parser = Parser(lexed)
            val ast = parser.parse()
            Interpreter.interpret(ast, args.drop(2).toList())
        }
        "execute" -> {
            val code = readSource(File(args[1]).readBytes())
            val lexed = Lexer.lex(code)
            val parser = Parser(lexed)
            val ast = parser.parse()
            Interpreter.interpret(ast, args.drop(2).toList())
        }
        "format" -> {
            val code = File(args[1]).readText().replace("\r", "")
            val result = StringBuilder("# [Fig](https://github.com/Seggan/Fig), ")
            if (code.any { it !in CODEPAGE }) {
                result.append(code.length)
            } else {
                result.append("\\$")
                result.append(code.length)
                result.append("\\log_{256}(")
                result.append(CODEPAGE.length)
                result.append(")\\approx\\$ ")
                val log = BDM.log(CODEPAGE.length.toBigDecimal(), MathContext.DECIMAL128) / BDM.log(
                    256.toBigDecimal(),
                    MathContext.DECIMAL128
                )
                result.append((log * code.length.toBigDecimal()).setScale(3, RoundingMode.HALF_UP).toPlainString())
            }
            result.appendLine(" bytes")
            result.appendLine("```")
            result.appendLine(code)
            result.appendLine("```")
            result.appendLine("[See the README to see how to run this](https://github.com/Seggan/Fig/blob/master/README.md)")
            val string = result.toString()
            Toolkit.getDefaultToolkit().systemClipboard.setContents(
                StringSelection(string),
                null
            )
            // already has a newline at the end
            print(string)
            println("Copied to clipboard")
        }
        else -> {
            println("Unknown command: ${args[0]}")
        }
    }
}

fun readSource(source: ByteArray): String {
    val b10 = bijectiveBaseDecode(source.map(Byte::toInt), Byte.MAX_VALUE + 1)
    val codepage = bijectiveBaseEncode(b10, CODEPAGE.length)
    return codepage.map { CODEPAGE[it] }.joinToString("")
}