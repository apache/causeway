package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

class RoToolPanel : SimplePanel() {

    val panel = VPanel()

    init {
        panel.width = CssSize(40, UNIT.px)
        panel.background = Background(Col.GREEN)
        val wButton: Button = Button(text = "Wikipedia", style = ButtonStyle.SUCCESS).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
        }
        panel.add(wButton)
    }
}
