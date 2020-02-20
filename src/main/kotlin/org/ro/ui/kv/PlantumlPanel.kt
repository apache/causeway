package org.ro.ui.kv

import org.ro.utils.UmlUtils
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.Direction
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.splitPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.px

@Deprecated("Useful as FlexSample")
object PlantumlPanel : VPanel() {

    var diagramPanel = vPanel(spacing = 3) {
        id = "diagramPanel"
        width = CssSize(100, UNIT.perc)
    }

    val textBox = TextArea(label = "Enter plantuml code here", rows = 20)
    private val okButton = Button("Create Diagram", "fas fa-check", ButtonStyle.SUCCESS).onClick {
        execute()
    }
    val sampleCode: String = "\"Bob -> Pita : sometimes is a\""
    val codePanel = vPanel {
        width = CssSize(400, UNIT.px)
        textBox.value = sampleCode
        add(textBox)
        add(okButton)
    }

    init {
        this.margin = 10.px
        this.minHeight = 400.px

        splitPanel(direction = Direction.VERTICAL) {
            codePanel
            diagramPanel
        }
    }

    private fun execute() {
        UmlUtils.generateDiagram(textBox.value!!, diagramPanel.id!!)
    }

}
