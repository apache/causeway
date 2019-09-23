package org.ro.bs3.to

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
