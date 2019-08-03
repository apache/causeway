package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.org.ro.core.observer.ErrorObserver
import org.ro.to.HttpError

@UnstableDefault
class HttpErrorHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer = ErrorObserver()
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(HttpError.serializer(), jsonStr)
    }
}
