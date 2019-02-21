package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.Action
import org.ro.to.Invokeable
import org.ro.to.Method

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
        val l = action.links[0]
        when (l.method) {
            Method.GET.name -> Invokeable(l.href, l.method).invoke()
            Method.POST.name -> Invokeable(l.href, l.method).invoke()  //FIXME  Prompt(action)
            Method.PUT.name -> {
                console.log("Link Method is PUT")
            }
            Method.PUT.name -> {
                console.log("Link Method is DELETE")
            }
        }
    }
}