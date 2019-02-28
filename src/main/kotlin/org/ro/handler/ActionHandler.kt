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
        val l = action.links[0]
        // l.rel should be neither: (self | up | describedBy )
        when (l.method) {
            Method.GET.name -> {
                console.log("[Link Method is GET]")
                console.log("[Link: $l]")
//                l.invoke()
            }
            Method.POST.name -> {
                console.log("[Link Method is POST]")
                l.invoke()
            }  //FIXME  Prompt(action)
            Method.PUT.name -> {
                console.log("Link Method is PUT")
            }
            Method.PUT.name -> {
                console.log("Link Method is DELETE")
            }
        }
    }

    fun parse(jsonStr: String): Action {
        return JSON.parse(Action.serializer(), jsonStr)
    }
}