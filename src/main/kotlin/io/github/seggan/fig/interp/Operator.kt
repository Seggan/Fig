package io.github.seggan.fig.interp

/**
 * @param symbol symbol of the operator
 * @param arity arity of the operator. -1 is variadic, -2 is unassigned
 * @param lazy if the evaluation of the arguments is delayed until control is passed to the operator
 */
enum class Operator(val symbol: String, val arity: Int = -2, val lazy: Boolean = false) {
    // tab newline and space are no-ops
    TERNARY_IF("!", 3, true),
    // " is strings
    // # is misc digraph char
    TODO_2("$"),
    MOD("%", 2),
    AND("&", 2),
    // ' is function ref
    MULTIPLY("*", 2),
    ADD("+", 2),
    PRINTLN(",", 1),
    SUBTRACT("-", 2),
    DIVIDE("/", 2),
    // 0-9 are digits
    PAIR(":", 1),
    PRINT_NO_NL(";", 1),
    LESS_THAN("<", 2),
    EQUAL("=", 2),
    GREATER_THAN(">", 2),
    IF_STATEMENT("?", 2, true),
    // @ is compressed string
    COMPRESS("#@", 1),
    ALL("A", 1),
    FROM_BINARY("B", 1),
    CHR_ORD("C", 1),
    // D is function definition
    TODO_8("E"),
    FILTER("F", 2),
    GENERATE("G", 2),
    TODO_10("H"),
    TODO_11("I"),
    TODO_12("J"),
    SORT("K", 1),
    LENGTH("L", 1),
    MAP("M", 2),
    NEGATE("N", 1),
    ODD("O", 1),
    TODO_16("P"),
    LAST_RETURN_VALUE("Q", 0),
    REDUCE("R", 2),
    SUM("S", 1),
    TODO_20("T"),
    TODO_21("U"),
    TODO_22("V"),
    TODO_23("W"),
    THIS_FUNCTION("X", 0),
    INTERLEAVE("Y", 2),
    TODO_26("Z"),
    TODO_27("["),
    // \ is char
    TODO_29("]"),
    POWER("^", 2),
    TODO_30("_"),
    LIST("`", -1),
    ANY("a", 1),
    TO_BINARY("b", 1),
    // c is a digraph char
    TODO_32("d"),
    VECTORISE_ON("e", 1, true),
    FLATTEN("f", 1),
    IS_FUNCTION("#f", 1),
    TODO_35("g"),
    TODO_36("h"),
    INDEX("i", 2),
    TODO_38("j"),
    TODO_39("k"),
    TODO_40("l"),
    IS_LIST("#l", 1),
    // m is a digraph char
    TODO_41("n"),
    IS_NUMBER("#n", 1),
    REMOVE("o", 2),
    TODO_43("p"),
    TODO_44("q"),
    EZR("r", 1),
    TODO_45("s"),
    IS_STRING("#s", 1),
    TODO_46("t"),
    TODO_47("u"),
    TODO_48("v"),
    WRAP_TWO("w", 2),
    INPUT("x", 0),
    PROGRAM_INPUT("#x", 0),
    TODO_51("y"),
    TODO_52("z"),
    TODO_53("{"),
    OR("|", 2),
    TODO_54("}"),
    TODO_55("~")
}

val CONSTANTS = buildMap<String, Any> {
    put("cH", "Hello, World!")
    put("cA", "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    put("ca", "abcdefghijklmnopqrstuvwxyz")
    put("cB", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
    put("cC", "BCDFGHJKLMNPQRSTVWXZ")
    put("cc", "bcdfghjklmnpqrstvwxz")
    put("cb", "BCDFGHJKLMNPQRSTVWXZbcdfghjklmnpqrstvwxz")
    put("cV", "AEIOU")
    put("cv", "aeiou")
    put("cO", "AEIOUaeiou")
    put("cY", "AEIOUY")
    put("cy", "aeiouy")
    put("co", "AEIOUYaeiouy")
    put("cD", "0123456789")
    put("cN", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")
    put("cn", "\n")
}.toMap()