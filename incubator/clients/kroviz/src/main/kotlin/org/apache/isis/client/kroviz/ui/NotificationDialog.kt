package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.ui.kv.RoStatusBar

class NotificationDialog(val message: String) : Command() {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        val fi = FormItem("Message", "TextArea", message, size = 5)
        fi.readOnly = true
        formItems.add(fi)
        val label = "Notifications"
        RoDialog(
                caption = label,
                items = formItems,
                command = this,
                widthPerc = 80).open()
    }

    override fun execute() {
        RoStatusBar.acknowledge()
    }

}
