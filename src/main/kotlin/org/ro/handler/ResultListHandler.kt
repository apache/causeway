package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ListAggregator
import org.ro.to.ResultList
import org.ro.to.TransferObject

class ResultListHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = ListAggregator(logEntry.title)
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultList.serializer(), jsonStr)
    }
}
