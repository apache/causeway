package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.ErrorDispatcher
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.to.TransferObject

class HttpErrorHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(ErrorDispatcher())
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(HttpError.serializer(), response)
    }
}
