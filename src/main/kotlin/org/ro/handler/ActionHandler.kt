package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Action
import org.ro.to.Method

class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val action = parse(jsonStr)
            val ext = action.extensions
            return ext.actionType.isNotEmpty()
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val action = parse(jsonStr)
        for (l in action.links) {
            // l.rel should be neither: (self | up | describedBy )
            if (l.isInvokeAction()) {
                console.log("[ActionHandler.doHandle] ${l.method}")
                console.log("[Link: $l]")
                when (l.method) {
                    Method.GET.name -> {
                        l.invoke()
                    }
                    Method.POST.name -> {
                        l.invoke()
                    }  //FIXME  Prompt(action)
                    Method.PUT.name -> {
                    }
                    Method.PUT.name -> {
                    }
                }
            }
        }
    }

    fun parse(jsonStr: String): Action {
        return JSON.parse(Action.serializer(), jsonStr)
    }
}