package io.github.seggan.fig.interp

enum class Operator(val symbol: String, val arity: Int = -2) {
    // tab newline and space are no-ops
    TERNARY_IF("!", 3),
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
    IF_STATEMENT("?", 3),
    // @ is compressed string
    COMPRESS("#@", 1),
    ALL("A", 1),
    TODO_6("B"),
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
    TODO_15("O"),
    TODO_16("P"),
    LAST_RETURN_VALUE("Q", 0),
    REDUCE("R"),
    SUM("S", 1),
    TODO_20("T"),
    TODO_21("U"),
    TODO_22("V"),
    TODO_23("W"),
    THIS_FUNCTION("X", 0),
    TODO_25("Y"),
    TODO_26("Z"),
    TODO_27("["),
    // \ is char
    TODO_29("]"),
    POWER("^", 2),
    TODO_30("_"),
    LIST("`", -1),
    ANY("a", 1),
    TODO_31("b"),
    // c is a digraph char
    TODO_32("d"),
    TODO_33("e"),
    TODO_34("f"),
    TODO_35("g"),
    TODO_36("h"),
    INDEX("i", 2),
    TODO_38("j"),
    TODO_39("k"),
    TODO_40("l"),
    // m is a digraph char
    TODO_41("n"),
    TODO_42("o"),
    TODO_43("p"),
    TODO_44("q"),
    EZR("r", 1),
    TODO_45("s"),
    TODO_46("t"),
    TODO_47("u"),
    TODO_48("v"),
    TODO_49("w"),
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
}.toMap()