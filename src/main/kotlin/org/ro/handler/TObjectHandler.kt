package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TObject
import org.ro.to.TransferObject


class TObjectHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(TObject.serializer(), response)
    }

}
