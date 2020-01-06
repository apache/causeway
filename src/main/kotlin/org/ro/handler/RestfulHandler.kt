package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.RestfulDispatcher
import org.ro.to.Restful
import org.ro.to.TransferObject

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
