package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class HttpErrorHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        val e = logEntry.getObj() as HttpError
        ErrorAlert(e).open()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(HttpError.serializer(), jsonStr)
    }
}