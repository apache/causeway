package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.to.Extensions
import org.ro.to.Property

class PropertyDescriptionHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonObj: JsonObject): Boolean {
        val id = jsonObj.get("id")
        if (id.isNull) {
            return false
        }
        val ext = Extensions(jsonObj)
        return ext.friendlyName.isNotEmpty()
    }

    override fun doHandle(jsonObj: JsonObject) {
        var p = Property(jsonObj)
        //FIXME logEntry.object = p
        //FIXME to be handled by Observers
        // Globals.getList().handleProperty(p)
    }
}