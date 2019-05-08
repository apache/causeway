package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.core.event.ListObserver
import org.ro.to.ResultList

class ResultListHandler : AbstractHandler(), IResponseHandler {

    //TODO structure of json is changed >= 16.2
    override fun doHandle() {
        val obs: ListObserver = ListObserver()
        logEntry.observer = obs
        obs.update(logEntry)
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(ResultList.serializer(), jsonStr)
    }
}