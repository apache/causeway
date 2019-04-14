package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Property

class PropertyDescriptionHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val p = parse(jsonStr)
            logEntry.obj = p
            val ext = p.extensions!!
            answer = ext.friendlyName.isNotEmpty()
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle() {
        console.log("[PropertyDescriptionHandler.doHandle()] has no body")
        //val p = logEntry.obj as Property
        //TODO logEntry.object = p to be handled by Observer?
    }

    fun parse(jsonStr: String) : Property {
        return JSON.parse(Property.serializer(), jsonStr)
    }
}