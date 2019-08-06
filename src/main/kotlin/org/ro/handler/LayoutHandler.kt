package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.layout.Layout
import org.ro.to.TransferObject

class LayoutHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Layout.serializer(), jsonStr)
    }

}
