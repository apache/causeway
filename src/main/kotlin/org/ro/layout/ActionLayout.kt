package org.ro.layout

import kotlinx.serialization.json.JsonObject

class ActionLayout(jsonObj: JsonObject? = null) : MemberLayout() {
     private var bookmarking: String? = null
     private var position: String? = null
     private var cssClassFa: String? = null
     private var cssClassFaPosition: String? = null

    init {
        bookmarking = jsonObj!!["bookmarking"].toString()
        position = jsonObj["position"].toString()
        cssClassFa = jsonObj["cssClassFa"].toString()
        cssClassFaPosition = jsonObj["cssClassFaPosition"].toString()
    }
}