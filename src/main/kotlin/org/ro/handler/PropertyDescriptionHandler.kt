package org.ro.handler

import org.ro.to.Property

class PropertyDescriptionHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        /*
        val id = JsonArray() //FIXME jsonObj.get("id")
        if (id.isNull) {
            return false
        }
        val ext = Extensions() //TODO
        return ext.friendlyName.isNotEmpty()  */
        return false
    }

    override fun doHandle(jsonStr: String) {
        var p = Property()//FIXME Property(jsonObj)
        //FIXME logEntry.object = p
        //FIXME to be handled by Observers
        // Globals.getList().handleProperty(p)
    }
}