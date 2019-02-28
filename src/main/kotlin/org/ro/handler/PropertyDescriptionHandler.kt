package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Property

class PropertyDescriptionHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        try {
            val p = parse(jsonStr)
            val ext = p.extensions!!
            return ext.friendlyName.isNotEmpty()
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val p = parse(jsonStr)
        logEntry.obj = p
        //TODO logEntry.object = p to be handled by Observer?
    }

    fun parse(jsonStr: String) : Property {
        return JSON.parse(Property.serializer(), jsonStr)
    }
}