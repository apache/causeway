package org.ro.to

import com.github.snabbdom._set
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.core.event.ILogEventObserver
import org.ro.core.event.RoXmlHttpRequest

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

    fun isInvokeAction(): Boolean {
        var answer = false
        if (rel.contains("invokeaction")) answer = true
        if (rel.contains("invoke;action")) answer = true
        return answer;
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

    fun setArgument(key: String?, value: String?) {
        console.log("[Link.setArgument] $key, $value")
        if (key != null) {
            val k = key.toLowerCase()
            if (k == "script") {
                val href = "{ \"href\": " + value + "}"
                arguments._set(k, href)
            } else {
                arguments._set(k, value)
            }
        }
        console.log("[Link.setArgument] $this")
    }

    fun getArgumentsAsJsonString(): String {
        val args = argMap()
        console.log("[Link.getArgumentsAsJsonString] $args")
        val answer =   JSON.stringify(argMap()!!.values)
        return answer
    }

}