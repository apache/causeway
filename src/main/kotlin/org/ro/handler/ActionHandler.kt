package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Action
import org.ro.to.Method
import org.ro.view.FormItem
import org.ro.view.RoDialog

class ActionHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false;
        try {
            val action = parse(jsonStr)
            val ext = action.extensions
            answer = ext.actionType.isNotEmpty()
        } catch (ex: Exception) {
        }
        return answer;
    }

    override fun doHandle(jsonStr: String) {
        val action = parse(jsonStr)
        console.log("[ActionHandler.doHandle] ${action}")
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
                        val formItems = mutableListOf<FormItem>()
                        formItems.add(FormItem("Script", "Text", ""))
                        formItems.add(FormItem("Parameters", "Text", ""))
                        //FIXME
                        RoDialog(formItems).show()
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