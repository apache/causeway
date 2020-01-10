package org.ro.ui.kv

import org.ro.ui.Command
import org.ro.ui.FormItem
import org.ro.ui.IconManager
import org.ro.ui.Point
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.VerticalAlign
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.window.Window

class RoDialog(
        caption: String,
        val items: List<FormItem>,
        val command: Command) :
        Window(caption, 600.px, 300.px, closeButton = true) {

    private val okButton = Button(
            caption,
            "fas fa-check",
            ButtonStyle.SUCCESS)
            .onClick {
                execute()
            }

    private val cancelButton = Button(
            "Cancel",
            "fas fa-times",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                close()
            }

    var panel: FormPanel<String>?

    init {
        icon = IconManager.find(caption)
        isDraggable = true
        isResizable = true
        closeButton = true
        verticalAlign = VerticalAlign.MIDDLE
        panel = FormPanelFactory(items).panel
        panel?.let { add(it) }

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

    fun open(at: Point = Point(100, 100)): Widget {
        left = CssSize(at.x, UNIT.px)
        top = CssSize(at.x, UNIT.px)
        UiManager.openDialog(this)
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
