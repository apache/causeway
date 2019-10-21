package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ObjectAggregator
import org.ro.to.ResultObject
import org.ro.to.TransferObject

class ResultObjectHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = ObjectAggregator(logEntry.title)
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultObject.serializer(), jsonStr)
    }

}
