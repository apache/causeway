package org.ro.ui

import org.ro.core.Utils
import org.ro.core.event.LogEntry
import org.ro.org.ro.ui.ImageAlert
import org.ro.ui.kv.RoDialog

class EventLogDetail(val logEntry: LogEntry) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Response", logEntry.url))
        var jsonStr = logEntry.response
        if (jsonStr.isNotEmpty()) {
            jsonStr = Utils.format(jsonStr)
        }
        formItems.add(FormItem("Text", "TextArea", jsonStr, 15))
        val label = logEntry.title
        val rd = RoDialog(caption = label, items = formItems, command = this)
        rd.open()
    }

    override fun execute() {
        ImageAlert().open()
    }

}
