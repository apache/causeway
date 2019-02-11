package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.generated.Property
import org.ro.to.Invokeable

@ImplicitReflectionSerializer
class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        console.log("[PropertyHandler fails on: $jsonStr]")
        val p = JSON.parse(Property.serializer(), jsonStr)
        val link = p.descriptionLink();
        return link != null
    }

    override fun doHandle(jsonStr: String) {
        val p = JSON.parse(Property.serializer(), jsonStr)
        val link = p.descriptionLink()!!;
        val i = Invokeable(link.href)
        i.invoke()
    }

}