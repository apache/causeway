package org.ro.view

import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel

object RoView : TabPanel() {
    init {
        marginTop = CssSize(-20, UNIT.px)
    }
}