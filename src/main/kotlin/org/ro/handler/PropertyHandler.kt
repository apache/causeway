package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.Invokeable
import org.ro.to.Property

@ImplicitReflectionSerializer
class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val p = JSON.parse(Property.serializer(), jsonStr)
            val link = p.descriptionLink();
            return link != null
        } catch (ex: Exception) {
            console.log("[PropertyHandler fails on: $jsonStr]")
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val p = JSON.parse(Property.serializer(), jsonStr)
        val link = p.descriptionLink()!!;
        val i = Invokeable(link.href)
        i.invoke()
    }

}