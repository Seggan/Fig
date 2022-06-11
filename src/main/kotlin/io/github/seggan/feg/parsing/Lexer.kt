package io.github.seggan.feg.parsing

object Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val iterator = input.iterator()
        while (iterator.hasNext()) {
            val c = iterator.nextChar()
            if (c in "\t\n\r ") {
                continue
            } else if (c == '"') {
                val result = buildString {
                    while (iterator.hasNext()) {
                        if (iterator.nextChar() == '"') break
                        append(iterator.nextChar())
                    }
                }
                tokens.add(Token(TokenType.STRING, result))
            } else if (c.code in 48..57) {
                val result = StringBuilder()
                result.append(c)
                while (iterator.hasNext()) {
                    val char = iterator.nextChar()
                    if (char.code in 48..57) {
                        result.append(c)
                    } else {
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