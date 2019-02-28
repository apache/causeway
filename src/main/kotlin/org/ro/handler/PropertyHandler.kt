package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Property

class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val p = parse(jsonStr)
            val link = p.descriptionLink();
            return link != null
        } catch (ex: Exception) {
            console.log("[PropertyHandler fails on: $jsonStr]")
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val p = parse(jsonStr)
        val link = p.descriptionLink()!!;
        link.invoke()
    }

    fun parse(jsonStr: String): Property {
        return JSON.parse(Property.serializer(), jsonStr)
    }

}