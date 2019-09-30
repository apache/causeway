package org.ro.to

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class Argument(var key: String = "",
                    @ContextualSerialization var value: Any? = null,
                    val potFileName: String = "") : TransferObject {
    init {
        if (value == null) {
            value = ""
        }
    }

    fun asBody(): String {
        var v = value!!
        if (v is String) {
            val isHttp = v.startsWith("http")
            v = quote(v)
            if (isHttp) {
                v = enbrace("href", v)
            }
            return quote(key) + ": " + enbrace("value", v)
        }
        return ""
    }

    private fun enbrace(k: String, v: String): String {
        return "{" + quote(k) + ": " + v + "}"
    }

    private fun quote(s: String): String {
        return "\"" + s + "\""
    }
}
