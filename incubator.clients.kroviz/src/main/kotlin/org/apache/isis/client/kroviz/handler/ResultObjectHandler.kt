package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.to.ResultObject
import org.apache.isis.client.kroviz.to.TransferObject

class ResultObjectHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(ObjectAggregator(logEntry.title))
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(ResultObject.serializer(), response)
    }

}
