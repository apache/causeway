package org.ro.core

import kotlinext.js.asJsObject
import pl.treksoft.kvision.panel.StackPanel
//import java.lang.Throwable

object Utils {

    fun debug(value: Any?, throwable: Throwable? = null) {
        val message: String?
        if (value is String) {
            message = value
        } else {
            message = value!!.asJsObject().toString()
        }
        console.log("[DEBUG] $message")
        if (throwable != null) {
            val stacktrace: Collection<StackPanel>? = null
            for (s in stacktrace!!) {
                console.log("\\t" + s + "\\n")
            }
        }
    }

    fun getSelfHref(jsonStr: String): String? {
        val startStr = "\"rel\": \"self\","
        val stopStr = "\"method\":"
        val startIndex = jsonStr.indexOf(startStr)
        val stopIndex = jsonStr.indexOf(stopStr)
        val selfHref = jsonStr.substring(startIndex, stopIndex)
        val stringList = selfHref.split("\"")
        val answer = stringList[2]//.substring(1, stringList[2].length-1)
        return answer
    }

    fun deCamel(input: String): String {
        var output = ""
        var i = 0
        for (c in input) {
            if (i == 0) {
                output += c.toUpperCase()
            } else {
                val o = if (c.toUpperCase() == c) {
                    " $c"
                } else {
                    c.toString()
                }
                output += o
            }
            i++
        }
        return output
    }

}
