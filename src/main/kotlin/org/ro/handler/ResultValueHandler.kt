package org.ro.org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.BaseHandler
import org.ro.handler.IResponseHandler
import org.ro.to.ResultValue
import org.ro.to.TransferObject

class ResultValueHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultValue.serializer(), jsonStr)
    }

}
