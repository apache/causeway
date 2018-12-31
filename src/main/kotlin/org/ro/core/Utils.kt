package org.ro.core

import kotlinx.serialization.json.JsonObject
import org.ro.to.Link
import pl.treksoft.kvision.modal.Alert
import pl.treksoft.kvision.utils.Object

class Utils {

    fun endsWith(obj: String, qry: String): Boolean {
        val qLen: Int = qry.length
        val oLen: Int = obj.length
        if (qLen > oLen) {
            return false
        }
        val end: String = obj.substring(oLen - qLen, oLen)
        return end == qry
    }

    fun camelCase(input: String): String {
        val firstChar: String = input[0].toString().toUpperCase()
        return firstChar + input.substring(1, input.length)
    }

    fun deCamel(input: String): String {
        var output = ""
        var i = 0
        var o = ""
        for (c in input) {
            if (i == 0) {
                output += c.toUpperCase()
            } else {
                o = if (c.toUpperCase() == c) {
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

    fun isEmptyObject(obj: JsonObject): Boolean {
        return JSON.stringify(obj).isEmpty()
    }

    // https://stackoverflow.com/questions/15816008/in-as3-how-to-check-if-two-json-objects-are-equal
    fun areEqual(a: JsonObject, b: JsonObject): Boolean {
        //FIXME
 /*       if (a === null || a is Number || a is Boolean || a is String) {
            // Compare primitive values.
            return a === b
        } else {
            var p: *
            for (p in a) {
                // Check if a and b have different values for p.
                if (!areEqual(a[p], b[p])) {
                    return false
                }
            }
            for (p in b) {
                // Check if b has a value which a does not.
                if (!a[p]) {
                    return false
                }
            }
            return true
        } */
        return false
    }

    fun replace(json: JsonObject, oldKey: String, newKey: String): JsonObject {
        val origin: String = JSON.stringify(json)
        if (origin.indexOf(oldKey) > 0) {
            val answer = origin.replace(oldKey, newKey)
            return JSON.parse(answer)
        }
        return json
    }

    fun getSelfHref(value: JsonObject): String? {
        //FIXME
     /*   var links: Array<Link> = value.links // rel==self
        for (l in links) {
            if (l.rel == "self") {
                return l.href
            }
        }  */
        return null
    }

    //TODO unify with getSelfHref, eventually use Builder pattern / fluent Interface 
    // (.fromString(response).linkNamed(SELF|UP|LAYOUT)
    fun getUpHref(response: String): String? {
        var obj: Object = JSON.parse(response)
        var links: Array<Link>? = null
        //FIXME
        /*
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

    fun toJsonString(jsonObj: JsonObject): String? {
        var jsonStr: String? = null
        try {
            jsonStr = JSON.stringify(jsonObj)
        } catch (err: Error) {
            Alert.show("Error: " + err.message)
        } finally {
            // Code that runs whether an error was thrown. This code can clean 
            // up after the error, or take steps to keep the application running. 
        }
        return jsonStr
    }

    fun htmlTip(jsonObj: Object): String {
        val str = JSON.stringify(jsonObj, null, 4)
        //FIXME var xml: String = XML(str)
        return str
    }

}