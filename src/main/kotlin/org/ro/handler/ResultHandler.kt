package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.event.NavigationObserver
import org.ro.to.Result
import org.ro.to.TransferObject

class ResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer = NavigationObserver()
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Result.serializer(), jsonStr)
    }

}
