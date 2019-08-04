package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.org.ro.core.observer.ActionObserver
import org.ro.to.Action
import org.ro.to.TransferObject

@UnstableDefault
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

