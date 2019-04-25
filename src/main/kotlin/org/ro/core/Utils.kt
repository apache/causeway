package org.ro.core

class Utils {

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

    companion object {
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

}