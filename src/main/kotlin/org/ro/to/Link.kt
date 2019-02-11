package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class Link(@SerialId(1) val method: String = "",
                @SerialId(2) val rel: String = "",
                @SerialId(3) val href: String = "",
                @SerialId(4) val type: String = "",
                @SerialId(5) @Optional val args: List<Argument> = emptyList(),
                @SerialId(6) @Optional val arguments: List<Argument> = emptyList(),
                @SerialId(7) @Optional val title: String = ""
) {
    //val argumentList: List<Arguments>? = null;

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