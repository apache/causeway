package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.generated.Action


class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        val action = JSON.parse(Action.serializer(), jsonStr)
        val ext = action.extensions
        return ext.actionType.isNotEmpty()
    }

    override fun doHandle(jsonStr: String) {
        val a = JSON.parse(Action.serializer(), jsonStr)
        console.log("[ActionHandler.doHandle -> Action] $a")
        val l = a.links!![0]
/*        when (l!!.method) {
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