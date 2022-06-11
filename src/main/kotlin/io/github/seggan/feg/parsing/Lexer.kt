package io.github.seggan.feg.parsing

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
                        } else {
                            append(char)
                        }
                    }
                }
                tokens.add(Token(TokenType.STRING, result))
            } else if (c.code in 48..57) {
                val result = StringBuilder()
                result.append(c)
                while (i < input.length) {
                    val char = input[i++]
                    if (char.code in 48..57 || char == '.') {
                        result.append(char)
                    } else {
                        i--
                        break
                    }
                }
                tokens.add(Token(TokenType.NUMBER, result.toString()))
            } else {
                tokens.add(
                    Token(
                        when (c) {
                            ';' -> TokenType.SEPARATOR
                            '(' -> TokenType.LAMBDA
                            ')' -> TokenType.CLOSER
                            '`' -> TokenType.LIST
                            else -> TokenType.OPERATOR
                        }, c.toString()
                    )
                )
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
    SEPARATOR,
    CLOSER,
    LIST,
    LAMBDA
}