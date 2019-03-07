package org.ro.to

import com.github.snabbdom._set
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.core.event.ILogEventObserver
import org.ro.core.event.RoXmlHttpRequest

enum class Method(val operation: String) {
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE")
}

enum class RelType(val type: String) {
    SELF("self"),
    UP("up"),
    DESCRIBEDBY("describedby")
}

@Serializable
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String = "",
                val type: String = "",
                @Optional val args: Map<String, Argument> = emptyMap(),
                @Optional val arguments: Map<String, Argument> = emptyMap(),
                @Optional val title: String = "") {

    //TODO eventually this function should be delegated
    fun invoke(obs: ILogEventObserver? = null) {
        RoXmlHttpRequest().invoke(this, obs)
    }
    
    fun isInvokeAction():Boolean {
        return rel.contains("invokeaction")
    }

    private fun argMap(): Map<String, Argument>? {
        if (args.isNotEmpty()) {
            return args
        }
        if (arguments.isNotEmpty()) {
            return arguments
        }
        return null
    }

    //TODO handle args as well?
    fun setArgument(key: String, value: String) {
        val k = key.toLowerCase()
        if (k == "script") {
            val href = "{ \"href\": " + value + "}"
            arguments._set(k, href)
        } else {
            arguments._set(k, value)
        }
    }

    fun getArgumentsAsJsonString(): String {
        return JSON.stringify(argMap()!!.values)
    }

}