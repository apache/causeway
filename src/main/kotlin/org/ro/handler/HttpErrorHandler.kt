package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.org.ro.core.observer.ErrorObserver
import org.ro.to.HttpError
import org.ro.to.TransferObject

class HttpErrorHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer = ErrorObserver()
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(HttpError.serializer(), jsonStr)
    }
}
