package org.ro.to

import kotlinx.serialization.json.JsonObject

class Service(jsonObj: JsonObject? = null) : TitledTO(jsonObj) {
    internal var valueList = mutableListOf<Invokeable>()
    internal var serviceId  = ""

    init {
        if (jsonObj != null) {
            serviceId = jsonObj["serviceId"].toString()
            val value = jsonObj["value"].jsonArray
            for (v in value) {
                valueList.add(Link(v as JsonObject))
            }
        }
    }

}