package org.ro.view.table.el

import org.ro.core.event.LogEntry
import org.ro.view.Command
import org.ro.view.FormItem
import org.ro.view.RoDialog

class EventLogDetail(val logEntry: LogEntry) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", logEntry.url))
        val jsonStr = logEntry.response
        formItems.add(FormItem("Text", "TextArea", toString(jsonStr), 20))
        val label = logEntry.title ?: "no label"
        RoDialog(label = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }

    //TODO how to pretty print?
    private fun toString(jsonStr: String): String {
        val s1 = JSON.parse<String>(jsonStr)
        val answer = JSON.stringify(s1)
        return answer
    }
}