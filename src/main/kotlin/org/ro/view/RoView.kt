package org.ro.view

import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc

object RoView : TabPanel() {
    init {
        width = 100.perc
        height = 100.perc
        marginLeft = auto
        marginRight = auto
        marginTop = auto //CssSize(0, UNIT.mm)
        marginBottom = auto
        maxHeight = 100.perc
    }
}