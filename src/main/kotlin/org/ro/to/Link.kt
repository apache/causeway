package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.core.model.Adaptable

class Link(jsonObj: JsonObject? = null) : Invokeable(jsonObj), Adaptable {
    var title = ""
    var rel = ""
    var type = ""
    private var args: JsonObject? = null // optional, http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke
    var arguments: JsonObject? = null
    var argumentList = mutableListOf<Argument>()

    init {
        if (jsonObj != null) {
            title = jsonObj["title"].toString()
            rel = jsonObj["rel"].toString()
            type = jsonObj["type"].toString()
            args = jsonObj["args"].jsonObject
            arguments = jsonObj["arguments"].jsonObject
            val arguments2 = jsonObj["arguments"].jsonArray
            if (arguments != null) {
                for (a in arguments2) {
                    argumentList.add(Argument(a as JsonObject))
                }
            }
        }
    }

    fun setArgument(key: String, value: String): Unit {
        val k: String = key.toLowerCase()
        if (k == "script") {
            var href = "{ \"href\": " + value + "}"
//FIXME            arguments[k).value = href
        } else {
//FIXME            arguments[k].value = value
        }
    }

    fun getArgumentsAsJsonString(): String {
        val obj = arguments as JsonObject
        return JSON.stringify(obj)
    }

    fun getHref(): String? {
        return "//FIXME"
    }

}