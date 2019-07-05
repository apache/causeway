package org.ro.handler

import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.org.ro.core.observer.ActionObserver
import org.ro.to.Action

class ActionHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.observer = ActionObserver()
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Action.serializer(), jsonStr)
    }
}

