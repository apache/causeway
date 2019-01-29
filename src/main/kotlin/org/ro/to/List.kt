package org.ro.to

import kotlinx.serialization.json.JsonObject

class List(jsonObj: JsonObject? = null) : LinkedTO() {
    private var resulttype: String? = null
    private var result: JsonObject? = null
    private var resultObject: Result? = null
    private var memberType: String? = null
    private var disabledReason: String? = null
    private var extensions: JsonObject? = null
    private var value: JsonObject? = null
    private var id: String? = null

    init {
        if (jsonObj != null) {
            val result = jsonObj["result"].jsonObject
            resultObject = Result(result)

            val links = jsonObj["links"].jsonArray
            val valueList: MutableList<Link> = mutableListOf()
            for (o in links) {
                valueList.add(Link(o as JsonObject))
            }
        }
    }

    fun getResult(): Result? {
        return resultObject
    }
    
}

