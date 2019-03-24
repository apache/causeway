package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Property

class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val p = parse(jsonStr)
            logEntry.obj = p
            val link = p.descriptionLink();
            answer = link != null
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
        val p = logEntry.obj as Property
        val link = p.descriptionLink()!!;
        link.invoke()
    }

    fun parse(jsonStr: String): Property {
        return JSON.parse(Property.serializer(), jsonStr)
    }

}