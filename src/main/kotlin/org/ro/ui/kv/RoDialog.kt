package org.ro.ui.kv

import org.ro.ui.Command
import org.ro.ui.IconManager
import org.ro.ui.Point
import org.ro.ui.uicomp.FormItem
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.core.VerticalAlign
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.Select
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.window.Window

class RoDialog(
        caption: String,
        init: (RoDialog.() -> Unit)? = null,
        val items: List<FormItem>,
        val command: Command) :
        Window(caption, 600.px, 300.px, closeButton = true) {

    private val okButton = Button(caption, "fas fa-check", ButtonStyle.SUCCESS).onClick {
        execute()
    }
    private val cancelButton = Button("Cancel", "fas fa-times", ButtonStyle.OUTLINEINFO).onClick {
        close()
    }

    var panel: FormPanel<String>?

    init {
        init?.invoke(this)
        icon = IconManager.find(caption)
        isDraggable = true
        isResizable = true
        closeButton = true
        verticalAlign = VerticalAlign.MIDDLE
        panel = formPanel {
            margin = 10.px
            for (fi: FormItem in items) {
                when (fi.type) {
                    "Text" -> {
                        add(Text(label = fi.label, value = fi.content as String))
                    }
                    "Password" -> {
                        add(Password(label = fi.label, value = fi.content as String))
                    }
                    "TextArea" -> {
                        val rowCnt = maxOf(3, fi.size)
                        add(TextArea(label = fi.label, value = fi.content as String, rows = rowCnt))
                    }
                    "Select" -> {
                        @Suppress("UNCHECKED_CAST")
                        val list = fi.content as List<StringPair>
                        var preSelectedValue: String? = null
                        if (list.isNotEmpty()) {
                            preSelectedValue = list.first().first
                        }
                        add(Select(label = fi.label, options = list, value = preSelectedValue))
                    }
                }
            }
        }
        val buttonBar = HPanel(spacing = 10) {
            margin = 10.px
        }
        buttonBar.add(okButton)
        buttonBar.add(cancelButton)
        add(buttonBar)
    }

    private fun execute() {
        command.execute()
        close()
    }

    fun show(at: Point): Widget {
        UiManager.openDialog(this, at)
        super.show()
        okButton.focus()
        return this
    }

    override fun close() {
        hide()
        super.remove(this)
        clearParent()
        dispose()
        panel = null
    }

}
