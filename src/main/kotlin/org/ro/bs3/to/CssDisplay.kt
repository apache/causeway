package org.ro.bs3.to

enum class CssDisplay {

    BLOCK,
    INLINE,
    INLINE_BLOCK;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): CssDisplay {
            return valueOf(v)
        }
    }

}
