package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int

//FIXME dynamic
class Property(jsonObj: JsonObject? = null) : Member(jsonObj) {
    val FRIENDLY_NAME = "friendlyName"
    val DESCRIBED_BY = "describedby"
    val RETURN_TYPE = "urn:org.restfulobjects:rels/return-type"

    private var parameters: JsonObject? = null
    private var maxLength: Int? = null

    init {
        memberType = PROPERTY
        if (jsonObj != null) {
            parameters = jsonObj["parameters"].jsonObject
            maxLength = jsonObj["maxLength"].int
        }
    }

    fun descriptionLink(): Link? {
        for (l in linkList) {
            if (l.rel == DESCRIBED_BY)
                return l
        }
        return null
    }

}