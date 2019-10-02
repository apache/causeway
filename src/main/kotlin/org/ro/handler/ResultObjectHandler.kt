package org.ro.org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.BaseHandler
import org.ro.handler.IResponseHandler
import org.ro.org.ro.to.ResultObject
import org.ro.to.TransferObject

class ResultObjectHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultObject.serializer(), jsonStr)
    }

}
