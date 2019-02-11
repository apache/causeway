package org.ro.core

import kotlinx.serialization.json.JsonObject
import pl.treksoft.kvision.modal.Alert

class Utils {

    fun camelCase(input: String): String {
        val firstChar: String = input[0].toString().toUpperCase()
        return firstChar + input.substring(1, input.length)
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

    fun getSelfHref(jsonStr: String): String? {
        val startStr = "\"rel\": \"self\","
        val stopStr = "\"method\":"
        val startIndex = jsonStr.indexOf(startStr)
        val stopIndex =   jsonStr.indexOf(stopStr)
        val selfHref = jsonStr.substring(startIndex, stopIndex) 
        val stringList = selfHref.split("\"")
        val answer = stringList[2]//.substring(1, stringList[2].length-1)
        return answer
    }

    //TODO unify with getSelfHref, eventually use Builder pattern / fluent Interface 
    // (.fromString(response).linkNamed(SELF|UP|LAYOUT)
    fun getUpHref(response: String): String? {
        //FIXME
        /*
        var obj: Object = JSON.parse(response)
        var links: Array<Link>? = null
        var value: Object = obj.value
        if (value is Array) {
            links = value as Array
        }
        if (links == null) {
            links = obj.links
        }
        for (l in links) {
            if (l.rel == "up") {
                return l.href
            }
        }     */
        return null
    }

    fun toJsonObject(jsonStr: String): JsonObject? {
        var jsonObject: JsonObject? = null
        try {
            jsonObject = JSON.parse(jsonStr)
        } catch (err: Error) {
            Alert.show("Error: " + err.message)
        } finally {
            // Code that runs whether an error was thrown. This code can clean 
            // up after the error, or take steps to keep the application running. 
        }
        return jsonObject
    }

}