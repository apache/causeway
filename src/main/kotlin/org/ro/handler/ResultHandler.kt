package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.core.event.NavigationObserver
import org.ro.to.Result

/** handles services result */
class ResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer = NavigationObserver()
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Result.serializer(), jsonStr)
    }
    
}