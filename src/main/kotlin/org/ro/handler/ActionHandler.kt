package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.ActionAggregator
import org.ro.to.Action
import org.ro.to.TransferObject

class ActionHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = ActionAggregator()
        logEntry.isRoot = true
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Action.serializer(), jsonStr)
    }
}

