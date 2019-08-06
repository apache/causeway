package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.event.ListObserver
import org.ro.to.ResultList
import org.ro.to.TransferObject

class ResultListHandler : BaseHandler(), IResponseHandler {

    //TODO structure of json is changed >= 16.2
    override fun doHandle() {
        logEntry.observer = ListObserver()
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(ResultList.serializer(), jsonStr)
    }
}
