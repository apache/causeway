package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.Command
import org.apache.isis.client.kroviz.ui.Displayable
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.ui.ImageDialog
import org.apache.isis.client.kroviz.utils.Direction
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Point
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
        heightPerc: Int = 100) :
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

    private val scaleUpButton = Button(
            "",
            "fas fa-plus",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                (command as ImageDialog).scale(Direction.UP)
            }

    private val scaleDownButton = Button(
            "",
            "fas fa-minus",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                (command as ImageDialog).scale(Direction.DOWN)
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

            add(formPanel!!, grow = 2)

            val buttonBar = HPanel(spacing = 10) {
                id = "button-bar"
                marginTop = 10.px
                marginLeft = 10.px
                marginBottom = 12.px
            }
            buttonBar.add(okButton)
            buttonBar.add(cancelButton)
            if (items.isNotEmpty() && hasScalableContent()) {
                buttonBar.add(scaleUpButton)
                buttonBar.add(scaleDownButton)
            }
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

    private fun hasScalableContent(): Boolean {
        val scalable = items.firstOrNull { it.type == ValueType.IMAGE.type }
        return scalable != null
    }

}
