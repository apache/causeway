package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.core.event.NavigationObserver
import org.ro.to.Result

/** handles services result */
class ResultHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        val obs = NavigationObserver()
        logEntry.observer = obs
        obs.update(logEntry)
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Result.serializer(), jsonStr)
    }
}