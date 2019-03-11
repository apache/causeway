package org.ro.view

import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.modal.Dialog
import pl.treksoft.kvision.utils.ENTER_KEY

class RoDialog(val items: List<FormItem>, val command: Command? = null) : Dialog<Any>(caption = "Execute", closeButton = true, escape = true, animation = true) {

    private var panel: FormPanel<String>? = null
    private val loginButton = Button("Login", "fa-check", ButtonStyle.PRIMARY).onClick {
        execute()
    }
    private val cancelButton = Button("Cancel", "fa-times", ButtonStyle.DEFAULT).onClick {
        this.dispose()
    }


    init {
        panel = formPanel {
            for (fi: FormItem in items) {
                add(Text(label = fi.label, value = fi.content))
            }
            setEventListener {
                keydown = {
                    if (it.keyCode == ENTER_KEY) {
                        execute()
                    }
                }
            }
        }
        addButton(cancelButton)
        addButton(loginButton)
    }

    private fun execute() {
        console.log("[RoDialog.execute]")
        command!!.execute()
        this.toggle()
        super.remove(this)
        this.dispose()
    }
}