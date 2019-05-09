package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.Service

class ServiceHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Service.serializer(), jsonStr)
    }

}