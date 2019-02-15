package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.Property

@ImplicitReflectionSerializer
class PropertyDescriptionHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        try {
            val p = JSON.parse(Property.serializer(), jsonStr)
            val ext = p.extensions!!
            return ext.friendlyName.isNotEmpty()
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val p = JSON.parse(Property.serializer(), jsonStr)
        //FIXME logEntry.object = p
        //FIXME to be handled by Observers
    }
}