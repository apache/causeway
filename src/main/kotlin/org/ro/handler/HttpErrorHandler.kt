package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ErrorAggregator
import org.ro.to.HttpError
import org.ro.to.TransferObject

class HttpErrorHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(ErrorAggregator())
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(HttpError.serializer(), response)
    }
}
