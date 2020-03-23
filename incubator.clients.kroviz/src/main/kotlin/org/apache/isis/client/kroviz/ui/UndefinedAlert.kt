package org.ro.view

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.Command
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.ui.kv.RoDialog

class UndefinedAlert(val logEntry: LogEntry) :Command {

    val instruction = """1. Create a ResponseClass under test/kotlin/org.ro.urls with
    - url 
    - str (JSON)
2. Create a TestCase under test/kotlin/org.ro.to
3. Implement a TransferObject under main/kotlin/org.ro.to
4. Implement a Handler under main/kotlin/org.ro.handler
5. Amend main/kotlin/org.ro.handler/ResponseHandler by this new Handler"""

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Instructions", "TextArea", instruction, size = 7))
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("JSON", "TextArea", logEntry.response, 10))
        val label = "TransferObject has no Handler"
       RoDialog(caption = label, items = formItems, command = this).open()
    }

    override fun execute() {
        //do nothing
    }
}
