package com.abs192.sudokai

class Entry(var flag: Type, var num: Int = 0) {

    init {
        if (flag != Type.EMPTY && num == 0)
            throw ExceptionInInitializerError("Pass non zero number as type is not EMPTY")
    }

    enum class Type {
        HARDCODED,
        MARKER,
        EMPTY,
        SOLUTION
    }

    override fun toString(): String {
        return "$num ${flag.name}"
    }
}