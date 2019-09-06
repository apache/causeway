package org.ro.to.bs3

enum class BookmarkPolicy {

    AS_ROOT,
    AS_CHILD,
    NEVER;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): BookmarkPolicy {
            return valueOf(v)
        }
    }

}
