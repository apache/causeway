package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.Action

@ImplicitReflectionSerializer
class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val action = JSON.parse(Action.serializer(), jsonStr)
            val ext = action.extensions
            return ext.actionType.isNotEmpty()
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val action = JSON.parse(Action.serializer(), jsonStr)
        console.log("[ActionHandler.doHandle -> Action] $action")
/*         val l = a.links!![0]
       when (l!!.method) {
            a.GET -> l.invoke()
            a.POST -> {
                //FIXME  Prompt(a)
            }  
            else -> //TODO handle PUT / DELETE
                // eventually in case of a DELETE, a confirmation needs to be shown
                console.log("Link Method is PUT or DELETE")
        }
        l.invoke()
        */
    }
}