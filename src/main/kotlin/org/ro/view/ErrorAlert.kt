package org.ro.view

class ErrorAlert(val label: String, val message:String) : Command {

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Message", "TextArea", message, 20))
        RoDialog(label = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }
}