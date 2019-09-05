package org.ro.org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ro.to.Argument
import org.ro.to.TransferObject
import org.ro.to.Value

/**
 *  Custom data structure to handle "args" and "arguments" used in Link.
 *  "arguments" can either be:
 *  @Item a map of Arguments (key/value pairs), eg. name: xxx, script: yyy or
 *  @Item a single Argument ('value')
 */
@Serializable
data class Arguments(
        @SerialName("value") val argument: Value? = null,
        val name: Argument? = null,
        val script: Argument? = null,
        val parameters: Argument? = null
) : TransferObject {

    fun isNotEmpty(): Boolean {
        return (argument != null || name != null || script != null)
    }

    fun asMap(): Map<String, Argument> {
        val map = mutableMapOf<String, Argument>()
        if (argument != null) {
            map["single"] = Argument(value = argument.toString())
        }
        if (name != null) {
            map["name"] = name
        }
        if (script != null) {
            map["script"] = script
        }
        if (parameters != null) {
            map["parameters"] = parameters
        }
        return map
    }

}

