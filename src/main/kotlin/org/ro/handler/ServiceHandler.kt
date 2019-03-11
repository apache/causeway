package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import org.ro.to.Service

class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            parse(jsonStr)
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
        val service = parse(jsonStr)
        Menu.add(service)
    }

    fun parse(jsonStr: String): Service {
        return JSON.parse(Service.serializer(), jsonStr)
    }

}