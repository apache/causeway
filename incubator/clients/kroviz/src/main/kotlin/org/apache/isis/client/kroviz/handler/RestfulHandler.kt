package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.RestfulDispatcher
import org.apache.isis.client.kroviz.to.Restful
import org.apache.isis.client.kroviz.to.TransferObject

class RestfulHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(RestfulDispatcher())
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Restful.serializer(), response)
    }

}
