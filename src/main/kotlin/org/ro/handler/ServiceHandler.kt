package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.generated.Services

class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        val services = JSON.parse(Services.serializer(), jsonStr)
        val hasExtensions = services.extensions != null
        //val hasExtensions = jsonStr.contains("\"extensions\":")
        console.log("[ServiceHandler has extensions]:" + hasExtensions)
        return hasExtensions
    }

    override fun doHandle(jsonStr: String) {
        val services = JSON.parse(Services.serializer(), jsonStr)
        console.log("[ServiceHandler doHandle]:" + services.toString())
        val values = services.value
        val menu = Menu(values!!.size)
        DisplayManager.setMenu(menu)
        for (l in values) {
            console.log("[ServiceHandler.doHandle -> invoke]: $l")
//FIXME            l.invoke()   make data class open and inherit from Invokeable
        }
    }

}