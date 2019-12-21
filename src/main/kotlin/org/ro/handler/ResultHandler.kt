package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.NavigationAggregator
import org.ro.to.ResultListResult
import org.ro.to.TransferObject

class ResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.addAggregator(NavigationAggregator())
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(ResultListResult.serializer(), response)
    }

}
