package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.layout.Layout

class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        val obs = logEntry.observer
        obs!!.update(logEntry)
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Layout.serializer(), jsonStr)
    }

}
