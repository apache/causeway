package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ListAggregator
import org.ro.to.InvocationResult
import org.ro.to.TransferObject

class InvocationResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = ListAggregator(logEntry.title)
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(InvocationResult.serializer(), jsonStr)
    }
}
