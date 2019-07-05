package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Argument(@Optional var key: String = "",
                    var value: String? = null,
                    @Optional val potFileName: String = "") : TransferObject {
    init {
        if (value == null) {
            value = ""
        }
    }

    fun asBody(): String {
        var v = value!!
        val isHttp = v.startsWith("http")
        v = quote(v)
        if (isHttp) {
            v = enbrace("href", v)
        }
        return quote(key) + ": " + enbrace("value", v)
    }

    private fun enbrace(k: String, v: String): String {
        return "{" + quote(k) + ": " + v + "}"
    }

    private fun quote(s: String): String {
        return "\"" + s + "\""
    }
}
