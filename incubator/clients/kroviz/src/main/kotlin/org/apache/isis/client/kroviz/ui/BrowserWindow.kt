package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.ui.kv.RoDialog

class BrowserWindow(val url: String) : Command() {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "IFrame", url))
        RoDialog(
                caption = url,
                items = formItems,
                command = this,
                defaultAction = "Visualize").open()
    }

}
