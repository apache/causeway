package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.to.Invokeable
import org.ro.to.Services

@ImplicitReflectionSerializer
class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            JSON.parse(Services.serializer(), jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val services =  JSON.parse(Services.serializer(), jsonStr)
        val values = services.valueList()
        val menu = Menu(values.size)
        DisplayManager.setMenu(menu)
        for (l in values) {
            console.log("[ServiceHandler.doHandle -> invoke]: $l")
            val i = Invokeable(l.href)
            i.invoke()
        }
    }

}