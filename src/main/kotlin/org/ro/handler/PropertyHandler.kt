package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.Property
import org.ro.to.TransferObject

class PropertyHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Property.serializer(), response)
    }

}
