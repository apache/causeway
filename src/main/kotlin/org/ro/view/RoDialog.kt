package org.ro.view

import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.select.Select
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.modal.Dialog
import pl.treksoft.kvision.utils.ENTER_KEY

class RoDialog(
        val label: String,
        val items: List<FormItem>,
        val command: Command) :
        Dialog<Any>(
                closeButton = true,
                animation = true,
                escape = true) { //TODO have it draggable&resizeable

    private var panel: FormPanel<String>? = null
    private val loginButton = Button("OK", "fa-check", ButtonStyle.SUCCESS).onClick {
        execute()
    }
    private val cancelButton = Button("Cancel", "fa-times", ButtonStyle.INFO).onClick {
        close()
    }

    init {
        caption = label
        panel = formPanel {
            for (fi: FormItem in items) {
                when (fi.type) {
                    "Text" -> {
                        add(Text(label = fi.label, value = fi.content))
                    }
                    "TextArea" -> {
                        add(TextArea(label = fi.label, value = fi.content, rows = 5))
                    }
                    "Select" -> {
                        add(Select(label = fi.label, value = fi.content))
                    }
                }
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
        command.execute()
        close()
    }

    private fun close() {
        this.toggle()
        super.remove(this)
        this.dispose()
    }
}