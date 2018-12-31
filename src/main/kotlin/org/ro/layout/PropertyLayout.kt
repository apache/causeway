package org.ro.layout

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import org.ro.to.Link

class PropertyLayout(jsonObj: JsonObject? = null) : MemberLayout() {
    var action: JsonObject? = null
    var labelPosition: String? = null
    var multiLine: Boolean? = null
    var renderedAsDayBefore: Boolean? = null
    var typicalLength: Int? = null
    var unchanging: JsonObject? = null

    init {
        if (jsonObj != null) {
            val link = jsonObj["link"].jsonObject
            linkObject = Link(link)
            //TODO link (sometimes) has an unexpected value of [object Object] - WTF
            action = jsonObj["action"].jsonObject
            labelPosition = jsonObj["labelPosition"].toString()
            multiLine = jsonObj["multiLine"].boolean
            renderedAsDayBefore = jsonObj["renderedAsDayBefore"].boolean
            typicalLength = jsonObj["typicalLength"].int
            unchanging = jsonObj["unchanging"].jsonObject
        }
    }

}