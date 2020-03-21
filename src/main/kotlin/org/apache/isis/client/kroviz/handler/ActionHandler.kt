package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.isis.client.kroviz.to.Action
import org.apache.isis.client.kroviz.to.TransferObject

class ActionHandler : BaseHandler() {

    override fun doHandle() {
        logEntry.addAggregator(ActionDispatcher())
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Action.serializer(), response)
    }
}

