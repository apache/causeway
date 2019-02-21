package org.ro.to

import com.github.snabbdom._set
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

    //TODO handle args as well?
    fun setArgument(key: String, value: String): Unit {
        val k = key.toLowerCase()
        if (k == "script") {
            val href = "{ \"href\": " + value + "}"
            arguments._set(k, href)
        } else {
            arguments._set(k, value)
        }
    }

    fun getArgumentsAsJsonString(): String {
        if (args != null) {
            return JSON.stringify(args)
        }
        if (arguments != null) {
            return JSON.stringify(arguments)
        }
        return "error"
    }

}