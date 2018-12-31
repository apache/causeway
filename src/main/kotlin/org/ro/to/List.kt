package org.ro.to

import kotlinx.serialization.json.JsonObject

class List(jsonObj: JsonObject? = null) : LinkedTO() {
     private var result: JsonObject? = null
     private lateinit var resultObject: Result

    init {
        if (jsonObj != null) {
            result = jsonObj.get("result").jsonObject
            resultObject = Result(result)
        }
    }

    fun getResult(): Result? {
        return resultObject
    }

}

