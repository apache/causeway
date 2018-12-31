package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.core.Utils
import org.ro.to.Service

class ServiceHandler : AbstractHandler(), IResponseHandler {
    
    override fun canHandle(jsonObj: JsonObject): Boolean {
        val extensions = jsonObj["extensions"].jsonObject
        return Utils().isEmptyObject(extensions)
    }

    override fun doHandle(jsonObj: JsonObject) {
        val service = Service(jsonObj)
        val values = service.valueList
        val menu = Menu(values.size)
        DisplayManager.setMenu(menu)
        for (l in values) {
            l.invoke()
        }
    }

}