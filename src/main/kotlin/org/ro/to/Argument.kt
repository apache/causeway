package org.ro.to

import kotlinx.serialization.Serializable

//IMPROVE initialize value="", move behavior out of TO
@Serializable
//TODO either have kotlinx.serialization cope with empty key or implement custom serialization (cf. to.Value)
data class Argument(var key: String = "",
                    var value: String? = null,
                    val potFileName: String = "") : TransferObject {
    init {
        if (value == null) {
            value = ""
        }
    }

    //TODO move to RoXMLHttpRequest / Helper class
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
