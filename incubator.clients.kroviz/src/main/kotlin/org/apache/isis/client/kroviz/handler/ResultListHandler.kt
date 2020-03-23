package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.ListAggregator
import org.apache.isis.client.kroviz.to.ResultList
import org.apache.isis.client.kroviz.to.TransferObject

class ResultListHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(ListAggregator(logEntry.title))
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(ResultList.serializer(), response)
    }

}
