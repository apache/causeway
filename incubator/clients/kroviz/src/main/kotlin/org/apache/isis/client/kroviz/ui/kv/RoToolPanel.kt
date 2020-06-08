package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.BrowserWindow
import org.apache.isis.client.kroviz.ui.IconManager
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

object RoToolPanel : SimplePanel() {

    val panel = VPanel()
    val buttons = mutableListOf<Button>()

    init {
        panel.marginTop = CssSize(40, UNIT.px)
        panel.width = CssSize(40, UNIT.px)
        panel.height = CssSize(100, UNIT.perc)
        panel.background = Background(color = Color.name(Col.LIGHTBLUE))
        panel.setDragDropData("text/plain", "element")
        initButtons()
        panel.addAll(buttons)
    }

    private fun initButtons() {
        val drop: Button = Button(
                text = "",
                icon = IconManager.find("Toolbox"),
                style = ButtonStyle.LIGHT).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = "Sample drop target"
            setDropTarget("text/plain") { data ->
                console.log("[RoToolPanel]")
                console.log(data)
                val obj = data.dataTransfer?.getData("text/plain")!!
                BrowserWindow("http://isis.apache.org").open()
            }
        }
        buttons.add(drop)
        //
        val drag = Button(
                text = "",
                icon = IconManager.find("Object"),
                style = ButtonStyle.LIGHT).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = "Sample drag object"
            setDragDropData("text/plain", "element")
        }
        buttons.add(drag)
    }

    fun toggle() {
        if (panel.width?.first == 0) show() else hide()
    }

    override fun hide(): Widget {
        panel.width = CssSize(0, UNIT.px)
        panel.removeAll()
        return super.hide()
    }

    override fun show(): Widget {
        panel.width = CssSize(40, UNIT.px)
        buttons.forEach { it -> panel.add(it) }
        return super.show()
    }

    fun addButton(obj: TObject) {

    }

}
