package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class HttpErrorHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        val e = logEntry.getObj() as HttpError
        ErrorAlert(e).open()
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(HttpError.serializer(), jsonStr)
    }
}