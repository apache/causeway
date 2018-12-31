package org.ro.to

import kotlinx.serialization.json.JsonObject

//TODO Design Issue: Does Result qualify as class?
class Result(jsonObj: JsonObject? = null) : LinkedTO() {
    internal var valueList: MutableList<Invokeable> = mutableListOf()
    private var extensions: JsonObject? = null
    private var extensionsObject: Extensions? = null

    init {
        if (jsonObj != null) {
            val value = jsonObj["value"].jsonArray
            for (v in value) {
                valueList.add(Link(v as JsonObject))
            }
            extensionsObject = Extensions(extensions)
        }
    }

}