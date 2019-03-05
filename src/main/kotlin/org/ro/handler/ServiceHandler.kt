package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import org.ro.to.Service

class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            parse(jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
//       console.log("[ServiceHandler.doHandle] \n$jsonStr")
        val service = parse(jsonStr)
        Menu.add(service)
    }

    fun parse(jsonStr: String): Service {
        return JSON.parse(Service.serializer(), jsonStr)
    }

}