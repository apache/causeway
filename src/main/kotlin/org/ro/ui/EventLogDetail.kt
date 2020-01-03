package org.ro.ui.table.el

import org.ro.core.event.LogEntry
import org.ro.ui.Command
import org.ro.ui.FormItem
import org.ro.ui.kv.RoDialog

class EventLogDetail(val logEntry: LogEntry) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", logEntry.url))
        var jsonStr = logEntry.response
        if (jsonStr.isNotEmpty()) {
            jsonStr = stringify(jsonStr)
        }
        formItems.add(FormItem("Text", "TextArea", jsonStr, 20))
        val label = logEntry.title
        RoDialog(caption = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }

    private fun stringify(jsonStr: String): String {
        val s1 = JSON.parse<String>(jsonStr)
        return JSON.stringify(s1, null, 2)
    }

}
