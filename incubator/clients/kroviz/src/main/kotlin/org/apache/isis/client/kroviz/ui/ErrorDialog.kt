package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.ui.kv.RoDialog

class ErrorDialog(val logEntry: LogEntry) : Command() {

    fun open() {
        val error = logEntry.getTransferObject() as HttpError
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("Message", "Text", error.message))
        val detail = error.detail
        if (detail != null) {
            formItems.add(FormItem("StackTrace", "TextArea", toString(detail.element), 15))
            formItems.add(FormItem("Caused by", "Text", detail.causedBy))
        }
        val label = "HttpError " + error.httpStatusCode.toString()
        RoDialog(
                caption = label,
                items = formItems,
                command = this,
                widthPerc = 80,
                heightPerc = 100).open()
    }

    private fun toString(stackTrace: List<String>): String {
        var answer = ""
        for (s in stackTrace) {
            answer += s + "\n"
        }
        return answer
    }

}
