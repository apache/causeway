package org.ro.bs3.to

enum class Size {

    XS,
    SM,
    MD,
    LG;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): Size {
            return valueOf(v)
        }
    }

}
