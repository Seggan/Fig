package io.github.seggan.fig.parsing

import io.github.seggan.fig.COMPRESSABLE_CHARS
import io.github.seggan.fig.COMPRESSION_CODEPAGE
import io.github.seggan.fig.DICTIONARY
import io.github.seggan.fig.interp.runtime.decompress

object Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i++]
            if (c in "\t\n\r ") {
                continue
            } else if (c == '"') {
                val result = buildString {
                    while (i < input.length) {
                        val char = input[i++]
                        if (char == '"') {
                            break
                        } else if (char == '\\') {
                            when (val nextChar = input[i++]) {
                                'n' -> append('\n')
                                'r' -> append('\r')
                                't' -> append('\t')
                                '\\' -> append('\\')
                                '"' -> append('"')
                                'u' -> {
                                    val hex = input.substring(i, i + 4)
                                    val codePoint = hex.toInt(16)
                                    append(codePoint.toChar())
                                    i += 4
                                }
                                else -> throw IllegalArgumentException("Invalid escape sequence: \\$nextChar")
                            }
                        } else {
                            append(char)
                        }
                    }
                }
                tokens.add(Token(TokenType.STRING, result))
            } else if (c == '\\') {
                tokens.add(Token(TokenType.STRING, input[i++].toString()))
            } else if (c == '@') {
                val result = buildString {
                    while (i < input.length) {
                        val char = input[i++]
                        if (char == '"') {
                            break
                        } else {
                            append(char)
                        }
                    }
                }
                tokens.add(Token(TokenType.STRING, decompress(result, COMPRESSION_CODEPAGE, COMPRESSABLE_CHARS, DICTIONARY)))
            } else if (c == '0') {
                tokens.add(Token(TokenType.NUMBER, "0"))
            } else if (c - '0' in 1..9) {
                val result = StringBuilder()
                result.append(c)
                while (i < input.length) {
                    val char = input[i++]
                    if (char - '0' in 0..9 || char == '.') {
                        result.append(char)
                    } else {
                        i--
                        break
                    }
                }
                tokens.add(Token(TokenType.NUMBER, result.toString()))
            } else if (c == '\\') {
                tokens.add(Token(TokenType.STRING, input[i++].toString()))
            } else {
                val type = when (c) {
                    '(' -> TokenType.LOOP
                    ')' -> TokenType.CLOSER
                    '\'' -> TokenType.FUNCTION_REFERENCE
                    'D' -> TokenType.DEFINITION
                    'U' -> TokenType.UNPACK_BULK
                    'u' -> TokenType.UNPACK
                    else -> TokenType.OPERATOR
                }
                tokens.add(Token(type, c.toString() + (if (c in "cm#") input[i++] else "")))
            }
        }
        return tokens
    }
}

data class Token(val type: TokenType, val value: String)

enum class TokenType {
    STRING,
    NUMBER,
    OPERATOR,
    CLOSER,
    LOOP,
    FUNCTION_REFERENCE,
    DEFINITION,
    UNPACK_BULK,
    UNPACK
}