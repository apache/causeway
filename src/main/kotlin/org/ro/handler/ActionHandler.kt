package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.Action
import org.ro.to.Method
import org.ro.view.ActionPrompt

class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        val action = logEntry.getObj() as Action
        for (l in action.links) {
            // l.rel should be neither: (self | up | describedBy )
            if (l.isInvokeAction()) {
                console.log("[ActionHandler.doHandle] ${l.method}")
                when (l.method) {
                    Method.GET.name -> {
                        l.invoke(logEntry.observer)
                    }
                    Method.POST.name -> {
                        ActionPrompt(action).open()
                    }
                    Method.PUT.name -> {
                    }
                    Method.PUT.name -> {
                    }
                }
            }
        }
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Action.serializer(), jsonStr)
    }
}