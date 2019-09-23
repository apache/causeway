package org.ro.bs3.to

enum class LabelPosition {

    DEFAULT,
    LEFT,
    RIGHT,
    TOP,
    NONE;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): LabelPosition {
            return valueOf(v)
        }
    }

}
