package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.Utils

class EventLogDetail(val logEntry: LogEntry) : Command() {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Response", logEntry.url))
        var jsonStr = logEntry.response
        if (jsonStr.isNotEmpty()) {
            jsonStr = Utils.format(jsonStr)
        }
        formItems.add(FormItem("Text", "TextArea", jsonStr, 15))
        val label = logEntry.title
        RoDialog(
                caption = label,
                items = formItems,
                command = this,
                defaultAction = "Visualize").open()
    }

}
