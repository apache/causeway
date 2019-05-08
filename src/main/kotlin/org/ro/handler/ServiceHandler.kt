package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.Service

class ServiceHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer!!.update(logEntry)
        //TODO shorten to logEntry.update() ?
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Service.serializer(), jsonStr)
    }

}