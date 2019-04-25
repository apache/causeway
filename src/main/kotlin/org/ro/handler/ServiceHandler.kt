package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Service

class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.obj = obj
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle() {
        logEntry.observer!!.update(logEntry)
        //TODO shorten to logEntry.update() ?
    }

    fun parse(jsonStr: String): Service {
        return JSON.parse(Service.serializer(), jsonStr)
    }

}