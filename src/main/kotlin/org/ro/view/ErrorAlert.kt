package org.ro.view

import org.ro.to.HttpError

class ErrorAlert(val error: HttpError) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Message", "Text", error.message))
        formItems.add(FormItem("StackTrace", "TextArea", toString(error.detail.element), 20))
        formItems.add(FormItem("Caused by", "Text", error.detail.causedBy))
        val label = "HttpError " + error.httpStatusCode.toString()
        RoDialog(label = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }
    
    private fun toString(stackTrace: List<String>):String {
        var answer =""
        for (s in stackTrace) {
            answer += s + "\n"
        }
        console.log("[ErrorAlert.toString] $answer")
        return answer
    }
}