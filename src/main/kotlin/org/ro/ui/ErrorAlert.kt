package org.ro.ui

import org.ro.core.event.LogEntry
import org.ro.to.HttpError
import org.ro.ui.uicomp.FormItem

class ErrorAlert(val logEntry: LogEntry) : Command {

    fun open() {
        val error = logEntry.getObj() as HttpError
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("Message", "Text", error.message))
        formItems.add(FormItem("StackTrace", "TextArea", toString(error.detail.element), 20))
        formItems.add(FormItem("Caused by", "Text", error.detail.causedBy))
        val label = "HttpError " + error.httpStatusCode.toString()
        RoDialog(label = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }

    private fun toString(stackTrace: List<String>): String {
        var answer = ""
        for (s in stackTrace) {
            answer += s + "\n"
        }
        return answer
    }

}
