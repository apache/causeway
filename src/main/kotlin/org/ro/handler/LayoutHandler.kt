package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.layout.Layout

class LayoutHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Layout.serializer(), jsonStr)
    }

}
