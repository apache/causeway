package org.ro.handler

import kotlinx.serialization.json.Json
import org.ro.core.TransferObject
import org.ro.to.Action
import org.ro.to.Link
import org.ro.to.Method
import org.ro.view.ActionPrompt

class ActionHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        // TODO move to observer
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

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Action.serializer(), jsonStr)
    }
}

fun Link.isInvokeAction(): Boolean {
    var answer = false
    if (rel.contains("invokeaction")) answer = true
    if (rel.contains("invoke;action")) answer = true
    return answer;
}

