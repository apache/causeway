package org.ro.to

import kotlinx.serialization.json.JsonObject

class Argument(jsonObj: JsonObject? = null) : BaseTO() {
    private lateinit var key: String
    private lateinit var value: JsonObject
    private lateinit var potFileName: String

    init {
        if (jsonObj != null) {
            key = jsonObj["key"].toString()
            value = jsonObj["value"].jsonObject
            potFileName = jsonObj["potFileName"].toString()
        }
    }

}