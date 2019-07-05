package org.ro.handler

import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.to.Service

class ServiceHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Service.serializer(), jsonStr)
    }

}
