package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.layout.Layout

@UnstableDefault
class LayoutHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Layout.serializer(), jsonStr)
    }

}
