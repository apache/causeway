package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.TObject

class TObjectHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer!!.update(logEntry)
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.nonstrict.parse(TObject.serializer(), jsonStr)
    }

}