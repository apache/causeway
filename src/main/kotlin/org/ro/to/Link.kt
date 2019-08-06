package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DEPRECATION")
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String = "",
                val type: String = "",
                @Optional val args: Map<String, Argument> = emptyMap(),
                @Optional val arguments: Map<String, Argument> = emptyMap(),
                @Optional val title: String = "") : TransferObject {

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
        if (key != null) {
            val k = key.toLowerCase()
            val v = value!!
            val arg = arguments.get(k)!!
            arg.key = k
            arg.value = v
        }
    }

    fun argumentsAsBody(): String {
        val args = argMap()!!
        val arg1 = args.get("script")!!
        val arg2 = args.get("parameters")!!
        return "{" + arg1.asBody() + "," + arg2.asBody() + "}"
    }

    fun isInvokeAction(): Boolean {
        var answer = false
        if (rel.contains("invokeaction")) answer = true
        if (rel.contains("invoke;action")) answer = true
        return answer;
    }

    fun resultKey():String {
        val parts = title.split(":")
        return parts[0]
    }

    fun resultTitle():String  {
        val start = title.indexOf(":")
        return title.substring(start + 1, title.length)
    }
}
