package org.ro.to.bs3

enum class CssClassFaPosition {

    LEFT,
    RIGHT;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): CssClassFaPosition {
            return valueOf(v)
        }
    }

}
