package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.BaseHandler
import org.ro.handler.IResponseHandler
import org.ro.to.Property
import org.ro.to.TransferObject

class VersionHandler: BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Property.serializer(), jsonStr)
    }

}
