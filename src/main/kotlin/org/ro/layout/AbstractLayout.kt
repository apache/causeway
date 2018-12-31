package org.ro.layout

import kotlinx.serialization.json.JsonObject

open class AbstractLayout(jsonObj: JsonObject? = null) {
    protected var debugInfo: JsonObject? = null
    private var cssClass: String? = null

    init {
        debugInfo = jsonObj!!["debugInfo"].jsonObject
        cssClass = jsonObj["cssClass"].toString()
    }

}