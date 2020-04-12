package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.*
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.FlexJustify
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px

class RoDialog(
        caption: String,
        val items: List<FormItem>,
        val command: Command,
        defaultAction: String = "OK",
        widthPerc: Int = 30,
        heightPerc: Int = 30) :
        Displayable, RoWindow(caption = caption, closeButton = true) {

    private val okButton = Button(
            text = defaultAction,
            icon = IconManager.find(defaultAction),
            style = ButtonStyle.SUCCESS)
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

    var formPanel: FormPanel<String>? = null

    init {
        icon = IconManager.find(caption)
        isDraggable = true
        isResizable = true
        closeButton = true
        contentWidth = CssSize(widthPerc, UNIT.perc)
        contentHeight = CssSize(heightPerc, UNIT.perc)

        vPanel(justify = FlexJustify.SPACEBETWEEN) {
            height = 100.perc
            formPanel = FormPanelFactory(items).panel
            formPanel?.height = 100.perc
            formPanel?.let {
                add(it, grow = 2)
            }

            val buttonBar = HPanel(spacing = 10) {
                id = "button-bar"
                margin = 10.px
            }
            buttonBar.add(okButton)
            buttonBar.add(cancelButton)
            add(buttonBar)
        }
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
    }

    fun hasScalableContent(): Boolean {
        val scalable = items.first { it.type == ValueType.IMAGE.type }
        return scalable != null
    }

}
