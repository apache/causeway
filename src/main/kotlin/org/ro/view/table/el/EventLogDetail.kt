package org.ro.view.table.el

import org.ro.core.event.LogEntry
import org.ro.view.Command
import org.ro.view.RoDialog
import org.ro.view.uicomp.FormItem

class EventLogDetail(val logEntry: LogEntry) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", logEntry.url))
        val jsonStr = logEntry.response
        formItems.add(FormItem("Text", "TextArea", toString(jsonStr), 20))
        val label = logEntry.title
        RoDialog(label = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }

    private fun toString(jsonStr: String): String {
        val s1 = JSON.parse<String>(jsonStr)
        return JSON.stringify(s1, null, 2)
    }

}
