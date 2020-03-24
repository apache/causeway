package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.DownloadDispatcher
import org.apache.isis.client.kroviz.to.ResultValue
import org.apache.isis.client.kroviz.to.TransferObject

class ResultValueHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(DownloadDispatcher(logEntry.title))
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(ResultValue.serializer(), response)
    }

}
