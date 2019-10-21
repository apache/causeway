package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.NavigationAggregator
import org.ro.to.ResultListResult
import org.ro.to.TransferObject

class ResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = NavigationAggregator()
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultListResult.serializer(), jsonStr)
    }

}
