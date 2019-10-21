package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.DownloadAggregator
import org.ro.to.ResultValue
import org.ro.to.TransferObject

class ResultValueHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = DownloadAggregator(logEntry.title)
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultValue.serializer(), jsonStr)
    }

}
