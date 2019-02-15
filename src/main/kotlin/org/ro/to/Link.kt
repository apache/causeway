package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Link(val rel: String = "",
                val method: String = "",
                val href: String = "",
                val type: String = "",
                @Optional val args: Map<String, Argument> = emptyMap(),
                @Optional val arguments: Map<String, Argument> = emptyMap(),
                @Optional val title: String = "") {

    fun setArgument(key: String, value: String): Unit {
        val k: String = key.toLowerCase()
        if (k == "script") {
//            var href = "{ \"href\": " + value + "}"
//FIXME            arguments[k).value = href
        } else {
//FIXME            arguments[k].value = value
        }
    }

    fun getArgumentsAsJsonString(): String {
        // return JSON.stringify(args)
        return "FIXME"
    }

}