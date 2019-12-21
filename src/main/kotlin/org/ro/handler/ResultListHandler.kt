package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ListAggregator
import org.ro.to.ResultList
import org.ro.to.TransferObject

class ResultListHandler : BaseHandler() {

    override fun doHandle() {
        console.log("[ResultListHandler.doHandle]")
        logEntry.addAggregator(ListAggregator(logEntry.title))
        console.log(logEntry)
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(ResultList.serializer(), response)
    }

}
