package org.ro.to.bs3

enum class PromptStyle {

    AS_CONFIGURED,
    DIALOG,
    INLINE,
    INLINE_AS_IF_EDIT;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): PromptStyle {
            return valueOf(v)
        }
    }

}
