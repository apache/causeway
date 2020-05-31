package org.ro.view

import org.apache.isis.client.kroviz.ui.Command
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.ui.kv.RoDialog

class NotificationDialog(val message: String) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Message", "TextArea", message, size = 7))
        val label = "Notifications"
        RoDialog(
                caption = label,
                items = formItems,
                command = this,
                widthPerc = 80,
                heightPerc = 100).open()
    }

    override fun execute() {
        //do nothing
    }
}
