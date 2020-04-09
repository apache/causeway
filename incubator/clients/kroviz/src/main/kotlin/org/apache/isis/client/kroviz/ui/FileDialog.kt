package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.ResultValue
import org.apache.isis.client.kroviz.ui.kv.RoDialog

class FileDialog(val logEntry: LogEntry) : Command {

    fun open() {
        val rv = logEntry.getTransferObject() as ResultValue
        val rvr = rv.result!!
        val value = rvr.value!!.content as String
        val list = value.split(":")
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("Blob", "TextArea", list[2], 15))
        val label = list[0] + "/" + list[1]
        RoDialog(caption = label, items = formItems, command = this).open()
    }

    override fun execute() {
        //do nothing
    }

}
