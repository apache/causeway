package org.ro.ui.kv

import org.ro.core.aggregator.DiagramAggregator
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.Method
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

object PlantumlPanel : VPanel() {

    val sampleCode: String = "\"Bob -> Pita : sometimes is a\""

    lateinit var diagramPanel: VPanel
    val textBox = TextArea(label = "Enter plantuml code here", rows = 20)

    private val okButton = Button("Create Diagram", "fas fa-check", ButtonStyle.SUCCESS).onClick {
        execute()
    }

    init {
        this.margin = 10.px
        this.minHeight = 400.px

        splitPanel(direction = Direction.VERTICAL) {
            vPanel {
                width = CssSize(400, UNIT.px)
                textBox.value = sampleCode
                add(textBox)
                add(okButton)
            }
            diagramPanel = vPanel(spacing = 3) {
                id = "diagramPanel"
            }
        }
    }

    // https://github.com/yuzutech/kroki
    private fun execute() {
        val url = "https://kroki.io/"
        val args = mutableMapOf<String, Argument>()
        args.put("diagram_source", Argument("\"diagram_source\"", textBox.value))
        args.put("diagram_type", Argument("\"diagram_type\"", "\"plantuml\""))
        args.put("output_format", Argument("\"output_format\"", "\"svg\""))

        val link = Link(href = url, method = Method.POST.operation, args = args)
        val agr = DiagramAggregator()
        RoXmlHttpRequest().processAnonymous(link, agr)
    }

}

