package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.to.Action
import org.ro.to.Extensions

class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        val ext: Extensions = asExtensions(jsonObj)
        return ext.actionType.isNotEmpty()
    }

    override fun doHandle(jsonObj: JsonObject) {
        val a = Action(jsonObj)
        val l = a.link
        when (l!!.method) {
            a.GET -> l.invoke()
            a.POST -> {
                //FIXME  Prompt(a)
            }
            else -> //TODO handle PUT / DELETE
                // eventually in case of a DELETE, a confirmation needs to be shown
                console.log("Link Method is PUT or DELETE")
        }
    }
}