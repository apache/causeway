package org.apache.isis.client.kroviz.ui.kv

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.handler.TObjectHandler
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.BrowserWindow
import org.apache.isis.client.kroviz.utils.IconManager
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

@OptIn(UnstableDefault::class)
object RoToolPanel : SimplePanel() {

    const val format = "object/model"
    val panel = VPanel()
    private val buttons = mutableListOf<Button>()

    init {
        panel.marginTop = CssSize(40, UNIT.px)
        panel.width = CssSize(40, UNIT.px)
        panel.height = CssSize(100, UNIT.perc)
        panel.background = Background(color = Color.name(Col.GHOSTWHITE))
        panel.setDropTarget(format) { data ->
            console.log("[RoToolPanel] panel")
            console.log(data)
            //TODO extract Exposer/TO from data
            val jsonStr = CFG.str
            val to = TObjectHandler().parse(jsonStr) as TObject
            val exp= Exposer(to)
            addButton(exp)
        }

        initButtons()
        panel.addAll(buttons)
    }

    private fun initButtons() {
        val drop: Button = buildButton("Toolbox", "Sample drop target")
        drop.setDropTarget(format) { data ->
            console.log("[RoToolPanel]")
            console.log(data)
            val obj = data.dataTransfer?.getData(format)!!
            BrowserWindow("http://isis.apache.org").open()
        }
        buttons.add(drop)
        //
        val drag = buildButton("Object", "Sample drag object")
        drag.setDragDropData(format, "element")
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

    private fun addButton(exp: Exposer) {
        val b = buildButton(exp.iconName, "dynamic sample")
        buttons.add(b)
        panel.add(b)
    }

    private fun buildButton(iconName: String, toolTip: String): Button {
        return Button(
                text = "",
                icon = IconManager.find(iconName),
                style = ButtonStyle.LIGHT).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = toolTip
        }
    }

}
